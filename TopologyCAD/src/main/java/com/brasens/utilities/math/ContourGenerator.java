package com.brasens.utilities.math;

import com.brasens.model.TopoLineType;
import com.brasens.model.objects.TopoObject;
import com.brasens.model.objects.TopoPoint;

import java.util.ArrayList;
import java.util.List;

public class ContourGenerator {

    public static List<TopoObject> generateContours(List<TopoPoint> points, double interval) {
        List<TopoObject> contours = new ArrayList<>();

        // Validação básica
        if (points.size() < 3 || interval <= 0) return contours;

        // 1. Gera a Malha TIN (Triangulated Irregular Network)
        List<Triangle> mesh = DelaunayTriangulator.triangulate(points);

        // 2. Determina Cota Mínima e Máxima do terreno
        double minZ = Double.MAX_VALUE;
        double maxZ = -Double.MAX_VALUE;
        for (TopoPoint p : points) {
            if (p.getZ() < minZ) minZ = p.getZ();
            if (p.getZ() > maxZ) maxZ = p.getZ();
        }

        // Arredonda o início para um múltiplo do intervalo (ex: se minZ=103.5 e interval=1, começa em 104.0)
        double startZ = Math.ceil(minZ / interval) * interval;

        // 3. Itera sobre cada cota (Z)
        for (double z = startZ; z <= maxZ; z += interval) {

            // Determina se é Curva Mestra (múltiplo de 5x o intervalo)
            // Ex: Se intervalo=1m, cotas 100, 105, 110 são mestras.
            boolean isMestra = Math.abs(z % (interval * 5)) < 0.001;

            TopoLineType tipoCurva = isMestra ? TopoLineType.CURVA_MESTRA : TopoLineType.CURVA_INTERMEDIARIA;
            String layerName = isMestra ? "CURVA_MESTRA" : "CURVA_NORMAL";

            // 4. Verifica interseção com CADA triângulo da malha
            for (Triangle t : mesh) {
                List<TopoPoint> intersections = new ArrayList<>();

                checkIntersection(t.p1, t.p2, z, intersections);
                checkIntersection(t.p2, t.p3, z, intersections);
                checkIntersection(t.p3, t.p1, z, intersections);

                // Um plano corta um triângulo em, no máximo, 2 pontos (formando um segmento)
                if (intersections.size() == 2) {
                    TopoObject segment = new TopoObject();
                    segment.setLayerName(layerName);
                    segment.setType(tipoCurva); // Define cor e espessura
                    segment.setClosed(false);

                    // Adiciona os pontos calculados
                    segment.addPoint(intersections.get(0));
                    segment.addPoint(intersections.get(1));

                    contours.add(segment);
                }
            }
        }
        return contours;
    }

    // Calcula onde a cota Z corta a aresta (p1-p2)
    private static void checkIntersection(TopoPoint p1, TopoPoint p2, double targetZ, List<TopoPoint> results) {
        double z1 = p1.getZ();
        double z2 = p2.getZ();

        double min = Math.min(z1, z2);
        double max = Math.max(z1, z2);

        // Se targetZ está fora do intervalo vertical da aresta, não cruza
        if (targetZ <= min || targetZ >= max) return;

        // Interpolação Linear: Onde está o Z ao longo da reta?
        double t = (targetZ - z1) / (z2 - z1);

        double x = p1.getX() + t * (p2.getX() - p1.getX());
        double y = p1.getY() + t * (p2.getY() - p1.getY());

        results.add(new TopoPoint("INT", x, y, targetZ));
    }
}