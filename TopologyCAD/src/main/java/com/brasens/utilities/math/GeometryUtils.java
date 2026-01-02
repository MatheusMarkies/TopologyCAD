package com.brasens.utilities.math;

import com.brasens.model.objects.TopoObject;
import com.brasens.model.objects.TopoPoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GeometryUtils {

    /**
     * Pega uma lista de objetos soltos (linhas/polilinhas) e tenta uni-los pelas pontas.
     * Retorna uma nova lista com os objetos unidos (e os que sobraram).
     */
    public static List<TopoObject> joinObjects(List<TopoObject> inputObjects) {
        if (inputObjects == null || inputObjects.isEmpty()) return new ArrayList<>();

        // 1. Cria uma lista de trabalho com cópias dos objetos (para não estragar os originais se der erro)
        List<TopoObject> pool = new ArrayList<>();
        for (TopoObject obj : inputObjects) {
            if (obj.getPoints().size() >= 2) {
                pool.add(cloneObj(obj));
            }
        }

        List<TopoObject> joinedList = new ArrayList<>();

        // 2. Loop de "Costura"
        while (!pool.isEmpty()) {
            TopoObject current = pool.remove(0);
            boolean modified = true;

            // Continua tentando grudar pedaços neste 'current' até não achar mais nada
            while (modified) {
                modified = false;

                // Pontas atuais do objeto que estamos construindo
                TopoPoint start = current.getPoints().get(0);
                TopoPoint end = current.getPoints().get(current.getPoints().size() - 1);

                for (int i = 0; i < pool.size(); i++) {
                    TopoObject candidate = pool.get(i);
                    TopoPoint cStart = candidate.getPoints().get(0);
                    TopoPoint cEnd = candidate.getPoints().get(candidate.getPoints().size() - 1);

                    boolean merged = false;

                    // Caso 1: Fim -> Início (A->B + B->C = A->B->C)
                    if (isSame(end, cStart)) {
                        current.getPoints().addAll(candidate.getPoints().subList(1, candidate.getPoints().size()));
                        merged = true;
                    }
                    // Caso 2: Fim -> Fim (A->B + C->B = A->B->C [Invertido])
                    else if (isSame(end, cEnd)) {
                        List<TopoPoint> rev = new ArrayList<>(candidate.getPoints());
                        Collections.reverse(rev);
                        current.getPoints().addAll(rev.subList(1, rev.size()));
                        merged = true;
                    }
                    // Caso 3: Início -> Fim (B->C + A->B = A->B->C [Prepend])
                    else if (isSame(start, cEnd)) {
                        List<TopoPoint> toAdd = candidate.getPoints().subList(0, candidate.getPoints().size() - 1);
                        current.getPoints().addAll(0, toAdd);
                        merged = true;
                    }
                    // Caso 4: Início -> Início (B->C + B->A = A->B->C [Reverse + Prepend])
                    else if (isSame(start, cStart)) {
                        List<TopoPoint> rev = new ArrayList<>(candidate.getPoints());
                        Collections.reverse(rev);
                        List<TopoPoint> toAdd = rev.subList(0, rev.size() - 1);
                        current.getPoints().addAll(0, toAdd);
                        merged = true;
                    }

                    if (merged) {
                        pool.remove(i); // Remove o pedaço usado da piscina
                        modified = true;
                        break; // Reinicia o loop interno para garantir integridade
                    }
                }
            }

            // 3. Aplica a validação mágica (Se fechou, vira PERIMETRO)
            current.validatePerimeter();
            joinedList.add(current);
        }

        return joinedList;
    }

    private static boolean isSame(TopoPoint p1, TopoPoint p2) {
        // Tolerância de 1cm para considerar conectado
        return Math.hypot(p1.getX() - p2.getX(), p1.getY() - p2.getY()) < 0.01;
    }

    private static TopoObject cloneObj(TopoObject src) {
        TopoObject obj = new TopoObject();
        obj.setId("JOIN-" + System.nanoTime()); // Novo ID
        obj.setLayerName(src.getLayerName());
        obj.setClosed(src.isClosed());
        for(TopoPoint p : src.getPoints()) {
            obj.addPoint(new TopoPoint(p.getName(), p.getX(), p.getY(), p.getZ()));
        }
        return obj;
    }
}