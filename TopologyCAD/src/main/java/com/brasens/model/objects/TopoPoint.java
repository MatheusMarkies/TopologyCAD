package com.brasens.model;

import lombok.*;

@Getter @Setter @AllArgsConstructor @ToString
public class TopoPoint {
    private String name; // Ex: "PT-1"
    private double x;    // Ex: 772665.235 (Este)
    private double y;    // Ex: 7998090.669 (Norte)
    private double z;    // Cota (Altura), opcional na fase 1
    private String desc; // Descrição (ex: "Cerca", "Eixo")
    private boolean selected = false;

    // Construtor simplificado para 2D
    public TopoPoint(String name, double x, double y) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.z = 0;
        this.desc = "";
    }
}
