package com.brasens.utilities.common.enums;

import lombok.Getter;

@Getter
public enum DataPeriod {

    MINUTE("Minuto"),
    HOUR("Hora"),
    FIVEHOUR("5 Horas"),
    DAILY("Diario"),
    WEEKLY("Semanal"),
    MONTHLY("Mensal"),
    ALL("Tudo");

    private final String legend;

    DataPeriod(String legend) {
        this.legend = legend;
    }

    public static DataPeriod getDataPeriod(String period) {
        switch (period) {
            case "Minuto":
                return DataPeriod.MINUTE;
            case "Hora":
                return DataPeriod.HOUR;
            case "5 Horas":
                return DataPeriod.FIVEHOUR;
            case "Diario":
                return DataPeriod.DAILY;
            case "Semanal":
                return DataPeriod.WEEKLY;
            case "Mensal":
                return DataPeriod.MONTHLY;
            case "Tudo":
                return DataPeriod.ALL;
            default:
                throw new IllegalArgumentException("Período desconhecido: " + period);
        }
    }
}
