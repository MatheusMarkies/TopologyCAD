package com.brasens.utilities.common;

import com.brasens.model.objects.TopoObject;
import com.brasens.model.objects.TopoPoint;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class SheetManager {


    public enum SheetFormat {
        // Largura, Altura, Margem(Dir/Sup/Inf), MargemEsq(Encadernação)
        A0(1189, 841, 10, 25),
        A1(841, 594, 10, 25),
        A2(594, 420, 7, 25),
        A3(420, 297, 7, 25),
        A4(210, 297, 7, 25);

        @Getter final double widthMm;
        @Getter final double heightMm;
        @Getter final double marginMm;
        @Getter final double marginLeftMm;

        SheetFormat(double w, double h, double m, double mL) {
            this.widthMm = w;
            this.heightMm = h;
            this.marginMm = m;
            this.marginLeftMm = mL;
        }
    }

    /**
     * Gera os objetos gráficos da folha na escala especificada com detalhamento ABNT.
     */
    public static List<TopoObject> createSheet(SheetFormat format, double scale, double originX, double originY) {
        List<TopoObject> objects = new ArrayList<>();

        // Fator: converter mm para metros na escala do desenho
        double factor = scale / 1000.0;

        double wWorld = format.widthMm * factor;
        double hWorld = format.heightMm * factor;
        double mLeft = format.marginLeftMm * factor;
        double mOther = format.marginMm * factor;

        // 1. LIMITE DO PAPEL (Borda Externa)
        TopoObject paper = createRect(originX, originY, wWorld, hWorld, "FOLHA-BORDA");
        paper.setLayerName("FOLHA");
        objects.add(paper);

        // 2. MARGENS (Área Útil de Desenho)
        TopoObject margins = new TopoObject();
        margins.setId("FOLHA-MARGEM");
        margins.setLayerName("FOLHA");
        margins.setClosed(true);

        // Coordenadas da margem interna
        double xMin = originX + mLeft;
        double xMax = originX + wWorld - mOther;
        double yMin = originY + mOther;
        double yMax = originY + hWorld - mOther;

        margins.addPoint(new TopoPoint("", xMin, yMin));
        margins.addPoint(new TopoPoint("", xMax, yMin));
        margins.addPoint(new TopoPoint("", xMax, yMax));
        margins.addPoint(new TopoPoint("", xMin, yMax));

        objects.add(margins);

        // 3. ESPAÇO PARA O SELO/CARIMBO (Canto Inferior Direito)
        // Padrão ABNT: Largura de 178mm (se A4) ou 175mm (outros), mas 178mm é universal para caber A4.
        double selloWidthMm = 178.0;
        double selloHeightMm = 100.0; // Altura sugerida, usuário ajusta depois inserindo o bloco

        // Se a folha for A4, o selo ocupa a largura total entre margens
        if (format == SheetFormat.A4) {
            selloWidthMm = format.widthMm - format.marginLeftMm - format.marginMm;
        }

        double sw = selloWidthMm * factor;
        double sh = selloHeightMm * factor;

        TopoObject seloBox = new TopoObject();
        seloBox.setId("FOLHA-SELO-AREA");
        seloBox.setLayerName("FOLHA");
        seloBox.setClosed(true);

        // O selo fica ancorado no canto inferior direito DA MARGEM
        double seloX = xMax - sw;
        double seloY = yMin; // Base da margem

        seloBox.addPoint(new TopoPoint("", seloX, seloY));
        seloBox.addPoint(new TopoPoint("", xMax, seloY));
        seloBox.addPoint(new TopoPoint("", xMax, seloY + sh));
        seloBox.addPoint(new TopoPoint("", seloX, seloY + sh));

        objects.add(seloBox);

        // 4. MARCAS DE DOBRA (Folding Marks) - Apenas para A0, A1, A2, A3
        // As marcas ficam na borda superior. O módulo de dobra é 210mm (largura A4).
        if (format != SheetFormat.A4) {
            double moduleA4 = 210.0 * factor;
            double tickSize = 5.0 * factor; // Tamanho do tracinho de marcação

            // Começa da esquerda para direita? Não, dobra-se a partir do selo (direita pra esquerda).
            // Vamos fazer marcas simples a cada 210mm (aproximado para visualização)

            int steps = (int) (format.widthMm / 210.0);
            for (int i = 1; i <= steps; i++) {
                double markX = originX + wWorld - (i * moduleA4);
                if (markX > originX) {
                    objects.add(createLine(
                            markX, originY + hWorld,
                            markX, originY + hWorld - tickSize,
                            "DOBRA-TOP-" + i
                    ));

                    // Marca na parte de baixo também (opcional, mas ajuda)
                    objects.add(createLine(
                            markX, originY,
                            markX, originY + tickSize,
                            "DOBRA-BOT-" + i
                    ));
                }
            }
        }

        // 5. MARCAS DE CENTRO (Cruzetas nas laterais)
        double tickCenter = 10.0 * factor;
        double midX = originX + (wWorld / 2.0);
        double midY = originY + (hWorld / 2.0);

        // Centro Superior
        objects.add(createLine(midX, originY + hWorld, midX, originY + hWorld - tickCenter, "CENTER-TOP"));
        // Centro Inferior
        objects.add(createLine(midX, originY, midX, originY + tickCenter, "CENTER-BOT"));
        // Centro Esquerdo
        objects.add(createLine(originX, midY, originX + tickCenter, midY, "CENTER-LEFT"));
        // Centro Direito
        objects.add(createLine(originX + wWorld, midY, originX + wWorld - tickCenter, midY, "CENTER-RIGHT"));

        return objects;
    }

    // --- Métodos Auxiliares ---

    private static TopoObject createRect(double x, double y, double w, double h, String id) {
        TopoObject obj = new TopoObject();
        obj.setId(id);
        obj.setLayerName("FOLHA");
        obj.setClosed(true);
        obj.addPoint(new TopoPoint("", x, y));
        obj.addPoint(new TopoPoint("", x + w, y));
        obj.addPoint(new TopoPoint("", x + w, y + h));
        obj.addPoint(new TopoPoint("", x, y + h));
        return obj;
    }

    private static TopoObject createLine(double x1, double y1, double x2, double y2, String id) {
        TopoObject obj = new TopoObject();
        obj.setId("MARK-" + id);
        obj.setLayerName("FOLHA");
        obj.setClosed(false);
        obj.addPoint(new TopoPoint("", x1, y1));
        obj.addPoint(new TopoPoint("", x2, y2));
        return obj;
    }
}