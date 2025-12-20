package com.brasens.utilities.math;

import com.brasens.model.objects.TopoPoint;
import java.util.ArrayList;
import java.util.List;

public class DelaunayTriangulator {

    public static List<Triangle> triangulate(List<TopoPoint> points) {
        List<Triangle> triangles = new ArrayList<>();
        if (points == null || points.size() < 3) return triangles;

        // 1. Determina os limites (Bounding Box) para criar o Super Triângulo
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE;

        for (TopoPoint p : points) {
            if (p.getX() < minX) minX = p.getX();
            if (p.getY() < minY) minY = p.getY();
            if (p.getX() > maxX) maxX = p.getX();
            if (p.getY() > maxY) maxY = p.getY();
        }

        double dx = maxX - minX;
        double dy = maxY - minY;
        double deltaMax = Math.max(dx, dy);
        double midX = (minX + maxX) / 2;
        double midY = (minY + maxY) / 2;

        // Super Triângulo deve ser grande o suficiente para englobar todos os pontos
        TopoPoint p1 = new TopoPoint("Super1", midX - 20 * deltaMax, midY - deltaMax);
        TopoPoint p2 = new TopoPoint("Super2", midX, midY + 20 * deltaMax);
        TopoPoint p3 = new TopoPoint("Super3", midX + 20 * deltaMax, midY - deltaMax);

        Triangle superTriangle = new Triangle(p1, p2, p3);
        triangles.add(superTriangle);

        // 2. Processa ponto a ponto
        for (TopoPoint p : points) {
            List<Triangle> badTriangles = new ArrayList<>();

            // Encontra triângulos que são invalidados pelo novo ponto
            for (Triangle t : triangles) {
                if (t.isInCircumcircle(p)) {
                    badTriangles.add(t);
                }
            }

            List<Edge> polygon = new ArrayList<>();

            // Encontra a fronteira do "buraco" deixado pelos triângulos ruins
            for (Triangle t : badTriangles) {
                addEdgeToPolygon(polygon, t.p1, t.p2);
                addEdgeToPolygon(polygon, t.p2, t.p3);
                addEdgeToPolygon(polygon, t.p3, t.p1);
            }

            triangles.removeAll(badTriangles);

            // Retriangula o buraco conectando ao novo ponto
            for (Edge edge : polygon) {
                triangles.add(new Triangle(edge.p1, edge.p2, p));
            }
        }

        // 3. Limpeza: Remove triângulos conectados ao Super Triângulo original
        triangles.removeIf(t -> t.p1 == p1 || t.p2 == p1 || t.p3 == p1 ||
                t.p1 == p2 || t.p2 == p2 || t.p3 == p2 ||
                t.p1 == p3 || t.p2 == p3 || t.p3 == p3);

        return triangles;
    }

    // Gerencia arestas para encontrar o contorno do polígono
    private static void addEdgeToPolygon(List<Edge> polygon, TopoPoint p1, TopoPoint p2) {
        for (int i = 0; i < polygon.size(); i++) {
            Edge edge = polygon.get(i);
            // Se a aresta já existe (em sentido inverso), é uma aresta interna -> remove
            if ((edge.p1 == p1 && edge.p2 == p2) || (edge.p1 == p2 && edge.p2 == p1)) {
                polygon.remove(i);
                return;
            }
        }
        // Se não existe, é aresta de borda -> adiciona
        polygon.add(new Edge(p1, p2));
    }

    private static class Edge {
        TopoPoint p1, p2;
        Edge(TopoPoint p1, TopoPoint p2) { this.p1 = p1; this.p2 = p2; }
    }
}