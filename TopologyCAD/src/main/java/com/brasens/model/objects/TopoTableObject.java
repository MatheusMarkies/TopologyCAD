package com.brasens.model.objects;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter
public class TopoTableObject extends TopoObject {

    // Lista de linhas de texto (Genérico)
    private List<String[]> dataRows = new ArrayList<>();
    private String[] headers = {"PONTO", "NORTE (Y)", "ESTE (X)", "COTA (Z)"};

    private double rowHeight = 5.0;
    private double colWidth = 20.0;
    private double fontSize = 2.5;

    public TopoTableObject(double originX, double originY) {
        super();
        this.setLayerName("TABELA");
        this.setId("TAB-" + System.currentTimeMillis());
        this.addPoint(new TopoPoint("ORIGIN", originX, originY));
    }

    // Construtor de compatibilidade (opcional, mas evita erro em outros lugares)
    public TopoTableObject(double originX, double originY, List<TopoPoint> points) {
        this(originX, originY);
        updateDataFromPoints(points);
    }

    public void updateDataFromPoints(List<TopoPoint> points) {
        this.headers = new String[]{"PONTO", "NORTE (Y)", "ESTE (X)", "COTA (Z)"};
        this.dataRows.clear();
        for (TopoPoint p : points) {
            String[] row = new String[] {
                    p.getName(),
                    String.format("%.2f", p.getY()),
                    String.format("%.2f", p.getX()),
                    String.format("%.2f", p.getZ())
            };
            dataRows.add(row);
        }
    }

    public void setCustomData(String[] headers, List<String[]> rows) {
        this.headers = headers;
        this.dataRows = rows;
    }

    public double getTotalWidth() {
        return colWidth * headers.length;
    }

    public double getTotalHeight() {
        return rowHeight * (1 + dataRows.size());
    }
}