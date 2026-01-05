package com.brasens.model.io;

import com.brasens.model.objects.TopoObject;
import com.brasens.model.objects.TopoPoint;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class DxfExport {

    public static void export(List<TopoObject> objects, File file) throws IOException {
        // Força Locale US para garantir que decimais usem ponto (.) e não vírgula
        Locale.setDefault(Locale.US);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writeHeader(writer);

            writer.write("0\nSECTION\n");
            writer.write("2\nENTITIES\n");

            for (TopoObject obj : objects) {
                if (obj.getPoints().isEmpty()) continue;

                String layer = (obj.getLayerName() != null) ? obj.getLayerName() : "0";

                // Ponto Isolado
                if (obj.getPoints().size() == 1) {
                    TopoPoint p = obj.getPoints().get(0);
                    // Exporta texto do nome do ponto
                    if (hasName(p)) {
                        writeText(writer, p.getX(), p.getY(), p.getName(), layer, 1.0);
                    }
                    writePoint(writer, p.getX(), p.getY(), layer);
                }
                // Linhas / Polígonos
                else {
                    writeLwPolyline(writer, obj, layer);

                    // Exporta os nomes dos vértices se existirem
                    for(TopoPoint p : obj.getPoints()) {
                        if (hasName(p)) {
                            writeText(writer, p.getX(), p.getY(), p.getName(), "TEXTO", 0.5);
                        }
                    }
                }
            }

            writer.write("0\nENDSEC\n");
            writer.write("0\nEOF\n");
        }
    }

    private static boolean hasName(TopoPoint p) {
        return p.getName() != null && !p.getName().isEmpty() && !p.getName().equals("null");
    }

    private static void writeHeader(BufferedWriter w) throws IOException {
        w.write("0\nSECTION\n");
        w.write("2\nHEADER\n");
        w.write("9\n$ACADVER\n1\nAC1009\n");
        w.write("0\nENDSEC\n");
    }

    private static void writePoint(BufferedWriter w, double x, double y, String layer) throws IOException {
        w.write("0\nPOINT\n");
        w.write("8\n" + layer + "\n");
        w.write("10\n" + String.format("%.4f", x) + "\n");
        w.write("20\n" + String.format("%.4f", y) + "\n");
        w.write("30\n0.0\n");
    }

    private static void writeText(BufferedWriter w, double x, double y, String text, String layer, double height) throws IOException {
        w.write("0\nTEXT\n");
        w.write("8\n" + layer + "\n");
        w.write("10\n" + String.format("%.4f", x) + "\n");
        w.write("20\n" + String.format("%.4f", y) + "\n");
        w.write("40\n" + height + "\n");
        w.write("1\n" + text + "\n");
    }

    private static void writeLwPolyline(BufferedWriter w, TopoObject obj, String layer) throws IOException {
        w.write("0\nLWPOLYLINE\n");
        w.write("8\n" + layer + "\n");
        w.write("90\n" + obj.getPoints().size() + "\n");
        w.write("70\n" + (obj.isClosed() ? "1" : "0") + "\n");

        for (TopoPoint p : obj.getPoints()) {
            w.write("10\n" + String.format("%.4f", p.getX()) + "\n");
            w.write("20\n" + String.format("%.4f", p.getY()) + "\n");
        }
    }
}