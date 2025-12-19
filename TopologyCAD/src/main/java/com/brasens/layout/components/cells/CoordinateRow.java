package com.brasens.layout.components.cells;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class CoordinateRow {
    private final StringProperty de;
    private final StringProperty para;
    private final StringProperty coordN;
    private final StringProperty coordE;
    private final StringProperty distancia;

    public CoordinateRow(String de, String para, double n, double e, double dist) {
        this.de = new SimpleStringProperty(de);
        this.para = new SimpleStringProperty(para);
        // Formatando para 3 casas decimais (Padrão Topografia)
        this.coordN = new SimpleStringProperty(String.format("%.3f", n));
        this.coordE = new SimpleStringProperty(String.format("%.3f", e));
        this.distancia = new SimpleStringProperty(String.format("%.3f m", dist));
    }

    public StringProperty deProperty() { return de; }
    public StringProperty paraProperty() { return para; }
    public StringProperty coordNProperty() { return coordN; }
    public StringProperty coordEProperty() { return coordE; }
    public StringProperty distanciaProperty() { return distancia; }
}