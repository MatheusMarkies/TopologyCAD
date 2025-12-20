package com.brasens.utilities.math;

import com.brasens.model.objects.TopoObject;
import com.brasens.model.objects.TopoPoint;

import java.util.List;

public class ScaleCalculator {

    private static final double[] STANDARD_SCALES = {
            50, 75, 100, 125, 150, 200, 250, 300, 400, 500, 750,
            1000, 1250, 1500, 2000, 2500, 3000, 4000, 5000, 7500,
            10000, 15000, 20000, 25000, 50000
    };

    public static double calculateBestScale(TopoObject object, PaperSize paperSize) {
        return calculateBestScale(List.of(object), paperSize);
    }

    public static double calculateBestScale(List<TopoObject> objects, PaperSize paperSize) {
        if (objects == null || objects.isEmpty()) return 100.0;

        // 1. Calcula o Bounding Box do mundo (em Metros)
        double minX = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;

        boolean hasPoints = false;

        for (TopoObject obj : objects) {
            for (TopoPoint p : obj.getPoints()) {
                hasPoints = true;
                if (p.getX() < minX) minX = p.getX();
                if (p.getX() > maxX) maxX = p.getX();
                if (p.getY() < minY) minY = p.getY();
                if (p.getY() > maxY) maxY = p.getY();
            }
        }

        if (!hasPoints) return 100.0;

        double worldWidth = maxX - minX;
        double worldHeight = maxY - minY;

        worldWidth *= 1.05;
        worldHeight *= 1.05;

        double paperWidthM = paperSize.getPrintableWidthMM() / 1000.0;
        double paperHeightM = paperSize.getPrintableHeightMM() / 1000.0;

        double scaleX = worldWidth / paperWidthM;
        double scaleY = worldHeight / paperHeightM;
        double rawScale1 = Math.max(scaleX, scaleY);

        double scaleX_rot = worldWidth / paperHeightM;
        double scaleY_rot = worldHeight / paperWidthM;
        double rawScale2 = Math.max(scaleX_rot, scaleY_rot);

        double bestRawScale = Math.min(rawScale1, rawScale2);

        return snapToStandardScale(bestRawScale);
    }

    private static double snapToStandardScale(double rawScale) {
        for (double std : STANDARD_SCALES) {
            if (std >= rawScale) {
                return std;
            }
        }
        return Math.ceil(rawScale / 5000.0) * 5000.0;
    }

    // --- ENUM PARA TAMANHOS DE PAPEL ---
    public enum PaperSize {
        A4(210, 297),
        A3(297, 420),
        A2(420, 594),
        A1(594, 841),
        A0(841, 1189);

        private final double widthMM;
        private final double heightMM;
        private final double marginMM = 15.0; // Margem de impressão (1.5cm)

        PaperSize(double widthMM, double heightMM) {
            this.widthMM = widthMM;
            this.heightMM = heightMM;
        }

        public double getPrintableWidthMM() {
            return widthMM - (marginMM * 2);
        }

        public double getPrintableHeightMM() {
            return heightMM - (marginMM * 2);
        }
    }
}