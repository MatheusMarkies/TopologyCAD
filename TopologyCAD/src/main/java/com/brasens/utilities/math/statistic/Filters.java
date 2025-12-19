package com.brasens.utilities.math.statistic;

public class Filters {
    public static class LowPassFilter {
        private final double alpha;

        public LowPassFilter(double alpha) {
            this.alpha = alpha;
        }

        public double[] apply(double[] data) {
            double[] output = new double[data.length];
            double[] input = data.clone();

            output[0] = alpha * input[1];

            for (int i = 1; i < data.length; ++i) {
                double filteredValue = alpha * input[i] + (1 - alpha) * output[i-1];
                output[i] = filteredValue;
            }

            return output;
        }
    }

    public static class HighPassFilter {
        private final double alpha;

        public HighPassFilter(double alpha) {
            this.alpha = alpha;
        }

        public double[] apply(double[] data) {
            double[] output = new double[data.length];
            double[] input = data.clone();

            output[0] = input[0];

            for (int i = 1; i < data.length; ++i) {
                double filteredValue = alpha * output[i-1] + alpha * (input[i] - input[i-1]);
                output[i] = filteredValue;
            }

            return output;
        }
    }
}
