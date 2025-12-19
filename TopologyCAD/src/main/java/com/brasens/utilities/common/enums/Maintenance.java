package com.brasens.utilities.common.enums;

import lombok.Getter;

@Getter
public enum Maintenance {
    PREDICTIVE("Preditivo"),
    PREVENTIVE("Preventivo"),
    SCHEDULEDCORRECTIVE("Corretivo Agendado"),
    CORRECTIVE("Corretivo");

    private final String legend;

    Maintenance(String legend) {
        this.legend = legend;
    }

    public static Maintenance getMaintenance(String period) {
        switch (period) {
            case "Preditivo":
                return Maintenance.PREDICTIVE;
            case "Preventivo":
                return Maintenance.PREVENTIVE;
            case "Corretivo Programado":
                return Maintenance.SCHEDULEDCORRECTIVE;
            case "Corretivo":
                return Maintenance.CORRECTIVE;
            default:
                throw new IllegalArgumentException("Maintenance: " + period);
        }
    }
}
