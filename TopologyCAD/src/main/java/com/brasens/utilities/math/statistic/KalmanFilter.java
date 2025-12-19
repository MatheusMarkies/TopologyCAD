package com.brasens.utilities.math.statistic;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author Matheus Markies
 */
@Getter @Setter @AllArgsConstructor
public class KalmanFilter {
    private double x; // estado do filtro
    private double p; // incerteza do estado
    private double q; // incerteza do processo
    private double r; // incerteza da medição

    /**
     * Atualiza o filtro de Kalman com uma nova medição.
     *
     * @param measurement Nova medição
     * @return Novo valor do estado após atualização
     */
    public double update(double measurement) {
        // Previsão do estado e da incerteza
        double xPredicted = x;
        double pPredicted = p + q;

        // Ganho de Kalman
        double k = pPredicted / (pPredicted + r);

        // Atualização do estado e da incerteza
        x = xPredicted + k * (measurement - xPredicted);
        p = (1 - k) * pPredicted;

        return x;
    }
}
