package com.brasens.model.objects;

import com.brasens.model.TopoLineType;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class TopoObject {
    private String id;
    private List<TopoPoint> points;
    private boolean closed;
    private String layerName = "DEFAULT";

    private TopoLineType type = TopoLineType.DEFAULT;

    public TopoObject() {
        this.points = new ArrayList<>();
        this.closed = false;
    }

    public TopoObject(List<TopoPoint> points, boolean closed) {
        this.points = points;
        this.closed = closed;
    }

    public void addPoint(TopoPoint p) {
        this.points.add(p);
    }

    // --- CORREÇÃO: Remove ponto duplicado ao validar fechamento ---
    public void validatePerimeter() {
        // 1. Verifica geometricamente se o último ponto é igual ao primeiro
        if (!points.isEmpty() && points.size() > 2) {
            TopoPoint p1 = points.get(0);
            TopoPoint p2 = points.get(points.size() - 1);

            double dist = Math.hypot(p1.getX() - p2.getX(), p1.getY() - p2.getY());

            // Se estiver no mesmo lugar (fechou o ciclo)
            if (dist < 0.01) {
                this.closed = true;
                // Remove o último ponto porque ele é redundante (já temos o primeiro)
                this.points.remove(points.size() - 1);
            }
        }

        // 2. Se a flag estiver fechada, define como Camada PERIMETRO
        if (this.closed) {
            this.setLayerName("PERIMETRO");
        }
    }
    // -------------------------------------------------------------

    public double getAreaHa() {
        if (this.points.size() < 3) return 0.0;

        if(!closed) return 0;

        double sum = 0.0;
        int n = points.size();
        for (int i = 0; i < n; i++) {
            TopoPoint p1 = points.get(i);
            TopoPoint p2 = points.get((i + 1) % n);
            sum += (p1.getX() * p2.getY()) - (p2.getX() * p1.getY());
        }
        return Math.abs(sum) / 2.0 / 10000.0;
    }

    public double getPerimeter() {
        if (points == null || points.size() < 2) {
            return 0.0;
        }

        double perimeter = 0.0;
        int n = points.size();

        for (int i = 0; i < n - 1; i++) {
            perimeter += distance(points.get(i), points.get(i + 1));
        }

        if (this.closed) {
            perimeter += distance(points.get(n - 1), points.get(0));
        }

        return perimeter;
    }

    private double distance(TopoPoint p1, TopoPoint p2) {
        return Math.hypot(p2.getX() - p1.getX(), p2.getY() - p1.getY());
    }
}