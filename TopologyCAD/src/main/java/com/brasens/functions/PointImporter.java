package com.brasens.functions;

import com.brasens.model.objects.TopoPoint;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PointImporter {

    public static List<TopoPoint> importFromCSV(File file) throws IOException {
        List<TopoPoint> points = new ArrayList<>();

        int sequence = 1;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();

                if (line.isEmpty() || line.startsWith("#") || line.startsWith("//") || line.toLowerCase().startsWith("id")) continue;

                String[] parts = line.split("[\\s,;]+");

                if (parts.length >= 2) {
                    try {
                        double x, y;
                        double z = 0;

                        if (parts.length == 2) {
                            x = Double.parseDouble(parts[0].replace(",", "."));
                            y = Double.parseDouble(parts[1].replace(",", "."));
                        } else {
                            x = Double.parseDouble(parts[1].replace(",", "."));
                            y = Double.parseDouble(parts[2].replace(",", "."));

                            if (parts.length > 3) {
                                try {
                                    z = Double.parseDouble(parts[3].replace(",", "."));
                                } catch (NumberFormatException ignored) {
                                }
                            }
                        }
                        String name = "PT-" + sequence++;
                        TopoPoint newPoint = new TopoPoint(name, x, y, z);

                        points.add(newPoint);

                    } catch (NumberFormatException e) {
                        System.err.println("Linha ignorada (formato numérico inválido): " + line);
                    }
                }
            }
        }
        return points;
    }
}