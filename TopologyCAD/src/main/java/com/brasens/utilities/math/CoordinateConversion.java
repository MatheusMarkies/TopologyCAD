package com.brasens.utilities.math;

public class CoordinateConversion {

    // Constantes WGS84
    private static final double A = 6378137.0;
    private static final double F = 1 / 298.257223563;
    private static final double K0 = 0.9996;

    public static double[] utmToLatLon(double x, double y, int zone, boolean isSouth) {
        double b = A * (1 - F);
        double e = Math.sqrt(1 - (b * b) / (A * A));
        double e0 = e / Math.sqrt(1 - e * e);

        double dx = x - 500000.0;
        double dy = isSouth ? y - 10000000.0 : y;

        double latRadM = dy / K0;

        double e1 = (1 - Math.sqrt(1 - e * e)) / (1 + Math.sqrt(1 - e * e));
        double m = latRadM / (A * (1 - e * e / 4 - 3 * Math.pow(e, 4) / 64 - 5 * Math.pow(e, 6) / 256));

        double mu = m;

        double phi1 = mu + (3 * e1 / 2 - 27 * Math.pow(e1, 3) / 32) * Math.sin(2 * mu)
                + (21 * Math.pow(e1, 2) / 16 - 55 * Math.pow(e1, 4) / 32) * Math.sin(4 * mu)
                + (151 * Math.pow(e1, 3) / 96) * Math.sin(6 * mu);

        double c1 = e0 * e0 * Math.pow(Math.cos(phi1), 2);
        double t1 = Math.pow(Math.tan(phi1), 2);
        double n1 = A / Math.sqrt(1 - Math.pow(e * Math.sin(phi1), 2));
        double r1 = A * (1 - e * e) / Math.pow(1 - Math.pow(e * Math.sin(phi1), 2), 1.5);
        double d = dx / (n1 * K0);

        double latRad = phi1 - (n1 * Math.tan(phi1) / r1) * (d * d / 2
                - (5 + 3 * t1 + 10 * c1 - 4 * c1 * c1 - 9 * e0 * e0) * Math.pow(d, 4) / 24
                + (61 + 90 * t1 + 298 * c1 + 45 * t1 * t1 - 252 * e0 * e0 - 3 * c1 * c1) * Math.pow(d, 6) / 720);

        double lonRad = (d - (1 + 2 * t1 + c1) * Math.pow(d, 3) / 6
                + (5 - 2 * c1 + 28 * t1 - 3 * c1 * c1 + 8 * e0 * e0 + 24 * t1 * t1) * Math.pow(d, 5) / 120)
                / Math.cos(phi1);

        double centralLon = (zone * 6 - 183) * (Math.PI / 180.0);
        lonRad += centralLon;

        return new double[]{ Math.toDegrees(latRad), Math.toDegrees(lonRad) };
    }
}