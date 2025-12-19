package com.brasens.utilities.common.enums;

import lombok.Getter;

@Getter
public enum PriorityState {
    URGENT("Urgente"),
    HIGH("Alta"),
    MIDDLE("Media"),
    LOW("Baixa");
    private final String legend;
    PriorityState(String legend) {
        this.legend = legend;
    }

    public static PriorityState getPriorityState(String period) {
        switch (period) {
            case "Urgente":
                return PriorityState.URGENT;
            case "Alta":
                return PriorityState.HIGH;
            case "Media":
                return PriorityState.MIDDLE;
            case "Baixa":
                return PriorityState.LOW;
            default:
                throw new IllegalArgumentException("Priority State desconhecido: " + period);
        }
    }
}
