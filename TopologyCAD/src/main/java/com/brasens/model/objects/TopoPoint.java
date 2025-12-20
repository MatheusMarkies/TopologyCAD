package com.brasens.model.objects;

import lombok.*;

@Getter @Setter @AllArgsConstructor @ToString
public class TopoPoint {
    private String name;

    private double x; // East
    private double y; // North

    // Coordenadas Geodésicas (Calculadas)
    private double latitude;
    private double longitude;

    // Altitude
    private double z = 0.0;

    // --- Dados Relativos ao Próximo Ponto (Link) ---
    // Usados para exibir na tabela (ex: P1 -> P2 tem azimute X e distância Y)
    private double distanceToNext = 0.0;
    private String azimuthToNext = "-";  // Ex: 125°30'10"
    private String rumoToNext = "-";     // Ex: 54°20' NE

    private boolean selected = false;

    public TopoPoint(String name, double x, double y) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.z = 0.0;
    }

    public TopoPoint(String name, double x, double y, double z) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    // Método auxiliar para obter coordenada formatada
    public String getLatFormatted() {
        return String.format("%.6f", latitude);
    }

    public String getLonFormatted() {
        return String.format("%.6f", longitude);
    }
}