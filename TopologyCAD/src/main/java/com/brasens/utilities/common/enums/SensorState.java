package com.brasens.utilities.common.enums;

public enum SensorState {
    WORKING("Ativo"), STANDBY("Parado");

    private final String legend;

    SensorState(String legend) {
        this.legend = legend;
    }

    public static SensorState getSensorState(String period) {
        switch (period) {
            case "Ativo":
                return SensorState.WORKING;
            case "Parado":
                return SensorState.STANDBY;
            default:
                throw new IllegalArgumentException("Período desconhecido: " + period);
        }
    }
}