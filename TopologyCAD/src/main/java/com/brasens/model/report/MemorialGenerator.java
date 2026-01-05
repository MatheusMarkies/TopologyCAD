package com.brasens.model.report;

import com.brasens.model.objects.TopoObject;
import com.brasens.model.objects.TopoPoint;
import com.brasens.utilities.math.TopologyMath;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.List;

public class MemorialGenerator {

    private static final DecimalFormat dfDist = new DecimalFormat("0.00");
    private static final DecimalFormat dfCoord = new DecimalFormat("0.000");

    public static void generateAndSave(TopoObject poly, ProjectData data, File file) throws IOException {
        if (poly == null || file == null) return;

        StringBuilder sb = new StringBuilder();

        // 1. CABEÇALHO
        sb.append("MEMORIAL DESCRITIVO\n\n");
        sb.append("IMÓVEL: ").append(data.getPropertyInfo().getNomePropriedade()).append("\n");
        sb.append("PROPRIETÁRIO: ").append(data.getPropertyInfo().getProprietario()).append("\n");
        sb.append("MUNICÍPIO: ").append(data.getPropertyInfo().getMunicipio()).append("\n");
        sb.append("ÁREA: ").append(String.format("%.4f ha", poly.getAreaHa())).append("\n");
        sb.append("PERÍMETRO: ").append(String.format("%.2f m", poly.getPerimeter())).append("\n\n");

        sb.append("DESCRIÇÃO DO PERÍMETRO:\n\n");

        // 2. CORPO DO TEXTO
        List<TopoPoint> points = poly.getPoints();
        int n = points.size();

        // Pega o primeiro ponto
        TopoPoint startP = points.get(0);

        sb.append("Inicia-se a descrição deste perímetro no vértice **")
                .append(startP.getName())
                .append("**")
                .append(", de coordenadas N=").append(dfCoord.format(startP.getY()))
                .append("m e E=").append(dfCoord.format(startP.getX())).append("m;\n");

        for (int i = 0; i < n; i++) {
            TopoPoint current = points.get(i);
            TopoPoint next = points.get((i + 1) % n); // Volta ao zero no final

            // Cálculos
            double dist = TopologyMath.getDistance2D(current, next);
            double azimute = TopologyMath.getAzimuth(current, next);
            String azimuteStr = TopologyMath.degreesToDMS(azimute);

            // Vizinho (Confrontante) armazenado no índice do ponto inicial do segmento
            String confrontante = poly.getConfrontante(i);
            if (confrontante == null || confrontante.isEmpty()) {
                confrontante = "quem de direito"; // Texto padrão jurídico se vazio
            }

            // Montagem da frase jurídica
            sb.append("Deste, segue confrontando com **")
                    .append(confrontante)
                    .append("**, com o azimute de ")
                    .append(azimuteStr)
                    .append(" e distância de ")
                    .append(dfDist.format(dist))
                    .append("m até o vértice **")
                    .append(next.getName())
                    .append("**")
                    .append(", de coordenadas N=").append(dfCoord.format(next.getY()))
                    .append("m e E=").append(dfCoord.format(next.getX())).append("m;\n");
        }

        // Fechamento
        sb.append("Fechando assim o perímetro acima descrito.\n\n");

        // 3. RODAPÉ (Assinatura)
        sb.append(data.getPropertyInfo().getMunicipio()).append(", ______ de __________________ de 20____.\n\n\n");
        sb.append("___________________________________________\n");
        sb.append(data.getProprietarioAssinatura().getNome()).append("\n");
        sb.append(data.getProprietarioAssinatura().getRegistroClass()).append("\n");

        // 4. GRAVAÇÃO DO ARQUIVO
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
            writer.write(sb.toString());
        }
    }
}