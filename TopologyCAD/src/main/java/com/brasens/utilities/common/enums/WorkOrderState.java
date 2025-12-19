package com.brasens.utilities.common.enums;

import lombok.Getter;

@Getter
public enum WorkOrderState {
    COMPLETED("Completo"),
    PLANNING("Planejado"),
    PROGRESS("Em Progresso"),
    OPENING("Aberto");

    private final String legend;

    WorkOrderState(String legend) {
        this.legend = legend;
    }

    public static WorkOrderState getWorkOrderState(String period) {
        switch (period) {
            case "Completo":
                return WorkOrderState.COMPLETED;
            case "Em planejamento":
                return WorkOrderState.PLANNING;
            case "Em progresso":
                return WorkOrderState.PROGRESS;
            case "Aberto":
                return WorkOrderState.OPENING;
            default:
                throw new IllegalArgumentException("WorkOrderState desconhecido: " + period);
        }
    }
}
