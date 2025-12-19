package com.brasens.utilities.analyses;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;

public class FeatureExtractor {

    public static final double G_TO_MS2 = 9.81;

    public static double[] performFFT(double[] input) {
        int nearestPowerOfTwo = findNearestPowerOfTwo(input.length);
        double[] resizedInput = new double[nearestPowerOfTwo];
        System.arraycopy(input, 0, resizedInput, 0, input.length);

        FastFourierTransformer fft = new FastFourierTransformer(DftNormalization.STANDARD);
        Complex[] complexResult = fft.transform(resizedInput, TransformType.FORWARD);
        double[] result = new double[complexResult.length];

        for (int i = 0; i < complexResult.length; i++) {
            result[i] = complexResult[i].abs();
        }

        return result;
    }

    private static int findNearestPowerOfTwo(int number) {
        int power = 1;
        while (power < number) {
            power <<= 1;
        }
        return power;
    }

    public static double[] calculateAcceleration(double[] axis){
        double[] accData = new double[axis.length];
        for(int i = 0;i< axis.length;i++){
            accData[i] = axis[i] * G_TO_MS2;
        }
        return accData;
    }

}