package com.brasens.utilities.math;

import com.brasens.model.objects.TopoPoint;

public class Triangle {
    public TopoPoint p1, p2, p3;

    public Triangle(TopoPoint p1, TopoPoint p2, TopoPoint p3) {
        this.p1 = p1;
        this.p2 = p2;
        this.p3 = p3;
    }

    public boolean isInCircumcircle(TopoPoint p) {
        double x1 = p1.getX(), y1 = p1.getY();
        double x2 = p2.getX(), y2 = p2.getY();
        double x3 = p3.getX(), y3 = p3.getY();

        double D = 2 * (x1 * (y2 - y3) + x2 * (y3 - y1) + x3 * (y1 - y2));

        // Evita divisão por zero (triângulos colineares/degenerados)
        if (Math.abs(D) < 1e-9) return false;

        double center_x = ((x1 * x1 + y1 * y1) * (y2 - y3) + (x2 * x2 + y2 * y2) * (y3 - y1) + (x3 * x3 + y3 * y3) * (y1 - y2)) / D;
        double center_y = ((x1 * x1 + y1 * y1) * (x3 - x2) + (x2 * x2 + y2 * y2) * (x1 - x3) + (x3 * x3 + y3 * y3) * (x2 - x1)) / D;

        double radiusSquared = (center_x - x1) * (center_x - x1) + (center_y - y1) * (center_y - y1);
        double distSquared = (center_x - p.getX()) * (center_x - p.getX()) + (center_y - p.getY()) * (center_y - p.getY());

        // Subtraímos uma tolerância pequena (epsilon) para robustez numérica
        return distSquared <= (radiusSquared - 1e-9);
    }

    // Verifica se compartilha uma aresta (usado na triangulação)
    public boolean sharesVertex(Triangle other) {
        return p1 == other.p1 || p1 == other.p2 || p1 == other.p3 ||
                p2 == other.p1 || p2 == other.p2 || p2 == other.p3 ||
                p3 == other.p1 || p3 == other.p2 || p3 == other.p3;
    }
}