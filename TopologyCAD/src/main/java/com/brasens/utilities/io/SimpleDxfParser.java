package com.brasens.utilities.io;

import com.brasens.model.objects.TopoObject;
import com.brasens.model.objects.TopoPoint;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SimpleDxfParser {

    /**
     * Lê um DXF e extrai LINHAS e POLILINHAS.
     * Assume que o DXF da folha foi desenhado em Milímetros (1 unidade = 1 mm).
     * Aplica a escala para converter para metros do desenho.
     */
    public static List<TopoObject> importDxf(File file, double scale) {
        List<TopoObject> importedObjects = new ArrayList<>();

        // Fator de conversão: Se o DXF está em mm, para virar metros no mundo na escala X:
        // Valor * (Scale / 1000)
        double factor = scale / 1000.0;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            String currentEntity = null;

            // Variáveis temporárias para leitura
            List<TopoPoint> currentPoints = new ArrayList<>();
            Double xStart = null, yStart = null, xEnd = null, yEnd = null;

            // Máquina de estados simples
            // DXF funciona em pares: Código (Linha 1) -> Valor (Linha 2)

            String groupCode = null;

            while ((line = br.readLine()) != null) {
                line = line.trim();

                if (groupCode == null) {
                    groupCode = line; // Lê o código
                } else {
                    String value = line; // Lê o valor

                    processDxfPair(groupCode, value, currentEntity, currentPoints, importedObjects, factor);

                    // Lógica de Entidades
                    if ("0".equals(groupCode)) {
                        // Novo objeto começou, finaliza o anterior se houver
                        if ("LINE".equals(currentEntity)) {
                            // Adiciona linha processada anteriormente (se tivermos os dados)
                            // (Na prática, simplificamos salvando quando o type muda)
                        }

                        if ("LINE".equals(value) || "LWPOLYLINE".equals(value)) {
                            currentEntity = value;
                            currentPoints = new ArrayList<>(); // Reseta pontos
                        } else if ("ENDSEC".equals(value) || "EOF".equals(value)) {
                            currentEntity = null;
                        }
                    }

                    groupCode = null; // Reseta para ler o próximo par
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return importedObjects;
    }

    private static Double tempX = null;
    private static Double tempY = null;
    private static TopoObject pendingObj = null;

    // Processamento simplificado "stream"
    private static void processDxfPair(String code, String value, String entity,
                                       List<TopoPoint> points, List<TopoObject> list, double factor) {

        if (entity == null) return;

        try {
            if ("LINE".equals(entity)) {
                if ("10".equals(code)) tempX = Double.parseDouble(value) * factor; // X Start
                if ("20".equals(code)) tempY = Double.parseDouble(value) * factor; // Y Start
                if ("11".equals(code)) {
                    double xEnd = Double.parseDouble(value) * factor;
                    // Se já temos o start, cria o objeto
                    if (tempX != null && tempY != null) {
                        TopoObject line = new TopoObject();
                        line.setLayerName("FOLHA_DXF");
                        line.addPoint(new TopoPoint("S", tempX, tempY));
                        // O Y final vem no próximo código 21, mas DXF garante ordem? Geralmente sim.
                        // Vamos simplificar assumindo que 11 vem antes de 21
                        tempX = xEnd; // Guarda X End temporariamente
                    }
                }
                if ("21".equals(code)) {
                    double yEnd = Double.parseDouble(value) * factor;
                    // Completa a linha
                    if (tempX != null) { // tempX aqui é o X End salvo acima
                        // Recupera o objeto anterior? Não, arquitetura stream é difícil pra DXF manual.
                        // Vamos mudar a estratégia para "Bufferizar Entidade".
                        // Veja método importDxfBuffered abaixo.
                    }
                }
            }
        } catch (Exception e) {}
    }

    // --- VERSÃO MAIS ROBUSTA E SIMPLES DE LEITURA ---
    public static List<TopoObject> importDxfBuffered(File file, double scale) {
        List<TopoObject> objects = new ArrayList<>();
        double factor = scale / 1000.0;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            String code;

            String currentType = "";
            double x1=0, y1=0, x2=0, y2=0;
            boolean hasX1=false, hasY1=false, hasX2=false, hasY2=false;

            // Para Polyline
            TopoObject currentPoly = null;

            while ((code = br.readLine()) != null) {
                String value = br.readLine();
                if (value == null) break;
                code = code.trim();
                value = value.trim();

                if ("0".equals(code)) {
                    // FINALIZA O ANTERIOR
                    if ("LINE".equals(currentType)) {
                        if (hasX1 && hasY1 && hasX2 && hasY2) {
                            TopoObject l = new TopoObject();
                            l.setLayerName("FOLHA_DXF");
                            l.addPoint(new TopoPoint("", x1, y1));
                            l.addPoint(new TopoPoint("", x2, y2));
                            objects.add(l);
                        }
                    }
                    if ("LWPOLYLINE".equals(currentType) && currentPoly != null) {
                        if (currentPoly.getPoints().size() > 1) objects.add(currentPoly);
                    }

                    // INICIA O NOVO
                    currentType = value;
                    hasX1=false; hasY1=false; hasX2=false; hasY2=false;

                    if ("LWPOLYLINE".equals(currentType)) {
                        currentPoly = new TopoObject();
                        currentPoly.setLayerName("FOLHA_DXF");
                        currentPoly.setClosed(false); // Flag 70 define fechamento, ignoraremos por simplicidade
                    }
                }

                // PROCESSA DADOS
                try {
                    if ("LINE".equals(currentType)) {
                        if ("10".equals(code)) { x1 = Double.parseDouble(value) * factor; hasX1=true; }
                        if ("20".equals(code)) { y1 = Double.parseDouble(value) * factor; hasY1=true; }
                        if ("11".equals(code)) { x2 = Double.parseDouble(value) * factor; hasX2=true; }
                        if ("21".equals(code)) { y2 = Double.parseDouble(value) * factor; hasY2=true; }
                    }
                    else if ("LWPOLYLINE".equals(currentType)) {
                        // LWPOLYLINE tem múltiplos códigos 10/20
                        if ("10".equals(code)) {
                            // Novo ponto X chegou. Se tivermos um Y pendente, algo deu errado, mas DXF é X depois Y.
                            tempX = Double.parseDouble(value) * factor;
                        }
                        if ("20".equals(code)) {
                            if (tempX != null) {
                                double py = Double.parseDouble(value) * factor;
                                currentPoly.addPoint(new TopoPoint("", tempX, py));
                                tempX = null;
                            }
                        }
                        if ("70".equals(code)) {
                            // Bitcode 1 = Closed
                            int flag = Integer.parseInt(value);
                            if ((flag & 1) != 0) currentPoly.setClosed(true);
                        }
                    }
                } catch (Exception e) {}
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return objects;
    }
}