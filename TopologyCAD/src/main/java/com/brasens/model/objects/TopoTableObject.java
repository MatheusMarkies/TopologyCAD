package com.brasens.model.objects;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter @Setter
public class TopoTableObject extends TopoObject {

    private List<TopoPoint> dataPoints; // Os pontos listados na tabela
    private double rowHeight = 5.0;     // Altura da linha (em metros/unidades do mundo)
    private double colWidth = 20.0;     // Largura padrão da coluna
    private double fontSize = 2.5;      // Tamanho da fonte (em metros)

    private final String[] headers = {"PONTO", "NORTE (Y)", "ESTE (X)", "COTA (Z)"};

    public TopoTableObject(double originX, double originY, List<TopoPoint> points) {
        super();
        this.dataPoints = points;
        this.setLayerName("TABELA");
        this.setId("TAB-" + System.currentTimeMillis());

        this.addPoint(new TopoPoint("ORIGIN", originX, originY));
    }

    public double getTotalWidth() {
        return colWidth * headers.length;
    }

    public double getTotalHeight() {
        return rowHeight * (1 + dataPoints.size());
    }
}