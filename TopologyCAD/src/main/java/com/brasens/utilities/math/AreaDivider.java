package com.brasens.utilities.math;

import com.brasens.model.objects.TopoObject;
import com.brasens.model.objects.TopoPoint;

import java.util.ArrayList;
import java.util.List;

public class AreaDivider {

    /**
     * Tenta dividir o polígono em duas partes, onde a primeira tem a área especificada (em Hectares).
     * @param polygon Polígono original (fechado)
     * @param targetAreaHa Área alvo em Hectares
     * @param azimuthGraus Ângulo da linha de corte (Azimute em graus)
     * @return Lista com 2 objetos (Área cortada, Área remanescente) ou lista vazia se falhar.
     */
    public static List<TopoObject> dividePolygon(TopoObject polygon, double targetAreaHa, double azimuthGraus) {
        // Validações básicas
        if (polygon == null || polygon.getPoints().size() < 3) return new ArrayList<>();

        double totalArea = polygon.getAreaHa();
        if (targetAreaHa <= 0 || targetAreaHa >= totalArea) {
            System.err.println("Área alvo inválida (deve ser menor que a área total).");
            return new ArrayList<>();
        }

        // 1. Rotaciona o polígono para que a linha de corte fique horizontal (y = constante)
        // Isso simplifica o problema para encontrar um "Y" de corte.
        // O Azimute é a partir do Norte (Y), sentido horário.
        // Para alinhar com o eixo X (0 graus matemático), rotacionamos -(90 - Azimute).
        // Simplificação: Vamos rotacionar tudo para que o Azimute vire eixo X.
        double rotationRad = Math.toRadians(-azimuthGraus);
        List<TopoPoint> rotatedPoints = rotatePoints(polygon.getPoints(), rotationRad);

        // 2. Determina os limites (Bounding Box) do polígono rotacionado
        double minY = Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;
        for (TopoPoint p : rotatedPoints) {
            if (p.getY() < minY) minY = p.getY();
            if (p.getY() > maxY) maxY = p.getY();
        }

        // 3. Busca Binária (Bisection) para encontrar a linha de corte Y
        double low = minY;
        double high = maxY;
        double tolerance = 1e-4; // Precisão de área (m²)
        int maxIter = 100;

        List<TopoPoint> bestCutPoly = null;
        List<TopoPoint> bestRemainderPoly = null;

        for (int i = 0; i < maxIter; i++) {
            double midY = (low + high) / 2.0;

            // Corta o polígono na altura Y (Horizontal pois rotacionamos)
            CutResult result = cutPolygonAtY(rotatedPoints, midY);

            if (result == null || result.bottomPart.size() < 3) {
                // Corte inválido (ex: fora do polígono ou pegou borda), tenta ajustar
                low = midY;
                continue;
            }

            // Calcula área da parte de baixo
            // Precisamos converter a área rotacionada de volta? Não, rotação preserva área.
            double currentAreaM2 = calculateAreaM2(result.bottomPart);
            double currentAreaHa = currentAreaM2 / 10000.0;

            if (Math.abs(currentAreaHa - targetAreaHa) < tolerance) {
                // Achou!
                bestCutPoly = result.bottomPart;
                bestRemainderPoly = result.topPart;
                break;
            } else if (currentAreaHa < targetAreaHa) {
                // Área muito pequena, precisamos subir a linha
                low = midY;
            } else {
                // Área muito grande, precisamos descer a linha
                high = midY;
            }
        }

        if (bestCutPoly != null) {
            // 4. Rotaciona de volta para as coordenadas originais
            List<TopoPoint> finalPart1 = rotatePoints(bestCutPoly, -rotationRad);
            List<TopoPoint> finalPart2 = rotatePoints(bestRemainderPoly, -rotationRad);

            List<TopoObject> result = new ArrayList<>();

            TopoObject obj1 = new TopoObject(finalPart1, true);
            obj1.setId(polygon.getId() + "-A");
            obj1.setLayerName(polygon.getLayerName());

            TopoObject obj2 = new TopoObject(finalPart2, true);
            obj2.setId(polygon.getId() + "-B");
            obj2.setLayerName(polygon.getLayerName());

            result.add(obj1);
            result.add(obj2);
            return result;
        }

        return new ArrayList<>();
    }

    // --- Classes Auxiliares e Métodos Privados ---

    private static class CutResult {
        List<TopoPoint> topPart;
        List<TopoPoint> bottomPart;

        public CutResult(List<TopoPoint> top, List<TopoPoint> bottom) {
            this.topPart = top;
            this.bottomPart = bottom;
        }
    }

    // Corta um polígono por uma linha horizontal infinita em Y
    private static CutResult cutPolygonAtY(List<TopoPoint> points, double y) {
        List<TopoPoint> bottomPoly = new ArrayList<>();
        List<TopoPoint> topPoly = new ArrayList<>();

        int n = points.size();

        // Algoritmo Sutherland-Hodgman simplificado para 1 linha de corte
        // Precisamos percorrer as arestas e verificar interseção com Y

        TopoPoint prev = points.get(n - 1);
        boolean prevIsBottom = prev.getY() <= y;

        // Precisamos gerenciar dois loops. Este método simplificado assume polígono convexo
        // ou corte simples que gera apenas 2 polígonos. Polígonos côncavos complexos podem falhar.

        // Nova abordagem: Coletar pontos de interseção e vértices
        List<TopoPoint> currentList = bottomPoly; // Começa assumindo que estamos em baixo? Não, depende do vértice.

        // Vamos usar lógica de "Current Side".
        // Side A: y <= cutY (Bottom). Side B: y > cutY (Top).

        // Lista temporária de todos os pontos (originais + interseções) marcados
        List<MarkedPoint> allPoints = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            TopoPoint curr = points.get(i);
            TopoPoint prevPt = (i == 0) ? points.get(n-1) : points.get(i-1);

            // Verifica interseção na aresta prev -> curr
            if ((prevPt.getY() <= y && curr.getY() > y) || (prevPt.getY() > y && curr.getY() <= y)) {
                // Houve cruzamento
                double xInt = prevPt.getX() + (y - prevPt.getY()) * (curr.getX() - prevPt.getX()) / (curr.getY() - prevPt.getY());
                allPoints.add(new MarkedPoint(xInt, y, true)); // Ponto de interseção
            }
            allPoints.add(new MarkedPoint(curr.getX(), curr.getY(), false));
        }

        // Se não houve interseção (ou tocou apenas 1 ponto), corte inválido
        long intersections = allPoints.stream().filter(p -> p.isIntersection).count();
        if (intersections < 2) return null;

        // Reconstrói os polígonos
        // Esta é a parte chata. Para polígonos convexos simples:
        // Os pontos abaixo de Y formam um polígono (com as interseções).
        // Os pontos acima formam outro.

        // Bottom Poly
        for (MarkedPoint p : allPoints) {
            if (p.y <= y) bottomPoly.add(p.toTopoPoint());
        }
        // Top Poly
        for (MarkedPoint p : allPoints) {
            if (p.y >= y) topPoly.add(p.toTopoPoint());
        }

        // Isso não funciona perfeitamente pois perde a ordem de fechamento.
        // Vamos tentar o método Sutherland-Hodgman Clássico para o Bottom Poly
        bottomPoly = clipPoly(points, y, true); // Keep Bottom
        topPoly = clipPoly(points, y, false);   // Keep Top

        return new CutResult(topPoly, bottomPoly);
    }

    // Sutherland-Hodgman para cortar polígono em Y
    private static List<TopoPoint> clipPoly(List<TopoPoint> subjectPoly, double clipY, boolean keepBottom) {
        List<TopoPoint> outputList = new ArrayList<>();

        int n = subjectPoly.size();
        TopoPoint prev = subjectPoly.get(n - 1);

        for (TopoPoint curr : subjectPoly) {
            boolean currInside = keepBottom ? (curr.getY() <= clipY) : (curr.getY() >= clipY);
            boolean prevInside = keepBottom ? (prev.getY() <= clipY) : (prev.getY() >= clipY);

            if (currInside) {
                if (!prevInside) {
                    // Entrou na região: Adiciona interseção
                    double xInt = prev.getX() + (clipY - prev.getY()) * (curr.getX() - prev.getX()) / (curr.getY() - prev.getY());
                    outputList.add(new TopoPoint("INT", xInt, clipY));
                }
                outputList.add(new TopoPoint(curr.getName(), curr.getX(), curr.getY()));
            } else if (prevInside) {
                // Saiu da região: Adiciona interseção
                double xInt = prev.getX() + (clipY - prev.getY()) * (curr.getX() - prev.getX()) / (curr.getY() - prev.getY());
                outputList.add(new TopoPoint("INT", xInt, clipY));
            }
            prev = curr;
        }
        return outputList;
    }

    private static List<TopoPoint> rotatePoints(List<TopoPoint> points, double angleRad) {
        List<TopoPoint> rotated = new ArrayList<>();
        double cos = Math.cos(angleRad);
        double sin = Math.sin(angleRad);

        for (TopoPoint p : points) {
            double xNew = p.getX() * cos - p.getY() * sin;
            double yNew = p.getX() * sin + p.getY() * cos;
            rotated.add(new TopoPoint(p.getName(), xNew, yNew));
        }
        return rotated;
    }

    private static double calculateAreaM2(List<TopoPoint> points) {
        double sum = 0.0;
        for (int i = 0; i < points.size(); i++) {
            TopoPoint p1 = points.get(i);
            TopoPoint p2 = points.get((i + 1) % points.size());
            sum += (p1.getX() * p2.getY()) - (p2.getX() * p1.getY());
        }
        return Math.abs(sum) / 2.0;
    }

    private static class MarkedPoint {
        double x, y;
        boolean isIntersection;
        public MarkedPoint(double x, double y, boolean isInt) { this.x=x; this.y=y; this.isIntersection=isInt; }
        public TopoPoint toTopoPoint() { return new TopoPoint("P", x, y); }
    }
}