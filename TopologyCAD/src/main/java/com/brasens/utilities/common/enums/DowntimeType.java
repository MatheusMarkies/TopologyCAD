package com.brasens.utilities.common.enums;

import lombok.Getter;

@Getter
public enum DowntimeType {
    MANUAL("Manual"),
    AUTOMATIC("Automatico");
    private final String legend;

    DowntimeType(String legend) {
        this.legend = legend;
    }

    public String getLegend() {
        return legend;
    }

    public static DowntimeType getDowntimeType(String period) {
        switch (period) {
            case "Manual":
                return DowntimeType.MANUAL;
            case "Automatico":
                return DowntimeType.AUTOMATIC;
            default:
                throw new IllegalArgumentException("DowntimeType desconhecido: " + period);
        }
    }
}
