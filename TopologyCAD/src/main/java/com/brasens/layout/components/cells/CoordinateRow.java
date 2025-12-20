package com.brasens.layout.components.cells;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class CoordinateRow {
    private final StringProperty de;
    private final StringProperty para;

    // Coordenadas Planas
    private final StringProperty coordN;
    private final StringProperty coordE;
    private final StringProperty coordZ; // Nova: Altitude

    // Dados da Aresta (Linha)
    private final StringProperty distancia;
    private final StringProperty azimute; // Nova
    private final StringProperty rumo;    // Nova

    // Coordenadas Geográficas
    private final StringProperty latitude;
    private final StringProperty longitude;

    public CoordinateRow(String de, String para,
                         double n, double e, double z,
                         double dist, String azimute, String rumo,
                         String latStr, String lonStr) {

        this.de = new SimpleStringProperty(de);
        this.para = new SimpleStringProperty(para);

        // Formatação UTM e Altitude (3 casas)
        this.coordN = new SimpleStringProperty(String.format("%.3f", n));
        this.coordE = new SimpleStringProperty(String.format("%.3f", e));
        this.coordZ = new SimpleStringProperty(String.format("%.3f", z));

        this.distancia = new SimpleStringProperty(String.format("%.3f m", dist));

        // Azimute e Rumo já vêm formatados (String) das funções matemáticas
        this.azimute = new SimpleStringProperty(azimute);
        this.rumo = new SimpleStringProperty(rumo);

        // Formatação Geográfica (6 a 8 casas para precisão)
        this.latitude = new SimpleStringProperty(latStr);
        this.longitude = new SimpleStringProperty(lonStr);
    }

    // Getters JavaFX
    public StringProperty deProperty() { return de; }
    public StringProperty paraProperty() { return para; }
    public StringProperty coordNProperty() { return coordN; }
    public StringProperty coordEProperty() { return coordE; }
    public StringProperty coordZProperty() { return coordZ; }
    public StringProperty distanciaProperty() { return distancia; }
    public StringProperty azimuteProperty() { return azimute; }
    public StringProperty rumoProperty() { return rumo; }
    public StringProperty latitudeProperty() { return latitude; }
    public StringProperty longitudeProperty() { return longitude; }
}