package com.brasens.model;

import javafx.scene.paint.Color;
import lombok.Getter;

@Getter
public enum TopoLineType {
    DEFAULT("Padrão", Color.CYAN, 1.5, null),

    // Hidrografia
    RIO("Rio", Color.BLUE, 2.0, null),
    CORREGO("Córrego", Color.CYAN, 1.5, new double[]{10, 5}),
    VALA("Vala", Color.MAGENTA, 1.5, null),
    BREJO("Brejo", Color.GREEN, 1.5, null), // Geralmente área, mas linha de contorno

    // Estruturas
    CERCA("Cerca", Color.DARKRED, 1.5, new double[]{15, 10}), // Traço longo, espaço
    MURO("Muro", Color.GRAY, 2.0, null),
    ALAMBRADO("Alambrado", Color.DARKORANGE, 1.5, new double[]{2, 5}), // Pontilhado

    // Viário
    ESTRADA("Estrada", Color.RED, 3.0, null), // Linha grossa
    RODOVIA("Rodovia", Color.ORANGERED, 4.0, null),
    FERROVIA("Ferrovia", Color.DARKRED, 2.0, new double[]{20, 5, 5, 5}), // Traço, ponto, traço

    CURVA_MESTRA("Curva Mestra", Color.ORANGE, 1.5, null), // A cada 5m (mais grossa)
    CURVA_INTERMEDIARIA("Curva Intermediária", Color.DARKGRAY, 0.8, null); // De 1 em 1m (fina)

    private final String label;
    private final Color color;
    private final double width;
    private final double[] dashArray; // Padrão do tracejado

    TopoLineType(String label, Color color, double width, double[] dashArray) {
        this.label = label;
        this.color = color;
        this.width = width;
        this.dashArray = dashArray;
    }

    @Override
    public String toString() {
        return label;
    }
}