package com.brasens.utilities.math;

import com.brasens.model.objects.TopoPoint;

public class TopologyMath {

    public static String formatLatitude(double latDecimal) {
        String direction = latDecimal >= 0 ? "N" : "S";
        return degreesToDMS(Math.abs(latDecimal)) + " " + direction;
    }

    /**
     * Formata Longitude Decimal para GMS (ex: 46°30'15" O)
     */
    public static String formatLongitude(double lonDecimal) {
        String direction = lonDecimal >= 0 ? "E" : "O"; // Ou 'W' se preferir inglês
        return degreesToDMS(Math.abs(lonDecimal)) + " " + direction;
    }

    /**
     * Calcula a Distância Horizontal (2D) entre dois pontos.
     */
    public static double getDistance2D(TopoPoint p1, TopoPoint p2) {
        return Math.hypot(p2.getX() - p1.getX(), p2.getY() - p1.getY());
    }

    /**
     * Calcula a Distância Inclinada (3D) considerando a diferença de nível.
     */
    public static double getDistance3D(TopoPoint p1, TopoPoint p2) {
        double d2d = getDistance2D(p1, p2);
        double dz = getDeltaZ(p1, p2);
        return Math.hypot(d2d, dz);
    }

    /**
     * Calcula a Diferença de Nível (DN).
     */
    public static double getDeltaZ(TopoPoint p1, TopoPoint p2) {
        return p2.getZ() - p1.getZ();
    }

    /**
     * Calcula o Azimute (em Graus Decimais) de P1 para P2.
     * Retorno: 0 a 360.
     */
    public static double getAzimuth(TopoPoint p1, TopoPoint p2) {
        double dy = p2.getY() - p1.getY();
        double dx = p2.getX() - p1.getX();

        // Math.atan2(x, y) em Topografia retorna o ângulo a partir do Norte (Eixo Y), sentido Horário.
        // Nota: A função padrão Java é atan2(y, x) (a partir do Leste, anti-horário).
        // Para corrigir para Topografia (Norte=0, Horário): usamos atan2(dx, dy).
        double rad = Math.atan2(dx, dy);

        double deg = Math.toDegrees(rad);

        if (deg < 0) {
            deg += 360.0;
        }

        return deg;
    }

    /**
     * Converte o Azimute para Rumo Formatado (ex: "45°00'00\" NE").
     */
    public static String getRumo(double azimuth) {
        azimuth = azimuth % 360;
        if (azimuth < 0) azimuth += 360;

        String quadrante;
        double angleRumo;

        if (azimuth >= 0 && azimuth < 90) {
            quadrante = "NE";
            angleRumo = azimuth;
        } else if (azimuth >= 90 && azimuth < 180) {
            quadrante = "SE";
            angleRumo = 180 - azimuth;
        } else if (azimuth >= 180 && azimuth < 270) {
            quadrante = "SW";
            angleRumo = azimuth - 180;
        } else { // 270 a 360
            quadrante = "NW";
            angleRumo = 360 - azimuth;
        }

        return degreesToDMS(angleRumo) + " " + quadrante;
    }

    /**
     * Formata graus decimais para String GMS (ex: 125°30'15").
     */
    public static String degreesToDMS(double degrees) {
        int d = (int) degrees;
        double remainder = (degrees - d) * 60;
        int m = (int) remainder;
        double s = (remainder - m) * 60;

        // Arredondamento para 2 casas decimais nos segundos
        return String.format("%d°%02d'%05.2f\"", d, m, s);
    }
}