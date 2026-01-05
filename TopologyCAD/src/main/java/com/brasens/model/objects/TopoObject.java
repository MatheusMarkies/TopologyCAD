package com.brasens.model.objects;

import com.brasens.model.TopoLineType;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
public class TopoObject {
    private String id;
    private List<TopoPoint> points;
    private boolean closed;
    private String layerName = "DEFAULT";

    private Map<Integer, String> confrontantes = new HashMap<>();

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

    public void validatePerimeter() {
        if (!points.isEmpty() && points.size() > 2) {
            TopoPoint p1 = points.get(0);
            TopoPoint p2 = points.get(points.size() - 1);

            double dist = Math.hypot(p1.getX() - p2.getX(), p1.getY() - p2.getY());

            if (dist < 0.01) {
                this.closed = true;
                this.points.remove(points.size() - 1);
            }
        }

        if (this.closed) {
            this.setLayerName("PERIMETRO");
        }
    }

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

    public String getConfrontante(int index) {
        return confrontantes.getOrDefault(index, null);
    }

    // Método para definir confrontante
    public void setConfrontante(int index, String nome) {
        this.confrontantes.put(index, nome);
    }

    public void setStartPointIndex(int newStartIndex) {
        if (points == null || points.isEmpty()) return;
        if (newStartIndex < 0 || newStartIndex >= points.size()) return;
        Collections.rotate(points, -newStartIndex);
    }

    public void reverseDirection() {
        if (points == null || points.size() < 3) return;

        List<TopoPoint> subList = points.subList(1, points.size());
        Collections.reverse(subList);
    }

    public void batchRename(String prefix, String separator, int startNumber) {
        int counter = startNumber;
        for (TopoPoint p : points) {
            // Formata com 2 dígitos se for menor que 10, senão normal
            String numStr = (counter < 10) ? "0" + counter : String.valueOf(counter);
            p.setName(prefix + separator + numStr);
            counter++;
        }
    }
}