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
                // Ignora linhas vazias ou comentários
                if (line.isEmpty() || line.startsWith("#") || line.startsWith("//")) continue;

                // Divide por qualquer espaço em branco
                String[] parts = line.split("\\s+");

                // Precisamos de pelo menos X e Y.
                // Se tiver ID no arquivo (parts >= 3), usamos X e Y das posições 1 e 2.
                // Se NÃO tiver ID (parts == 2), usamos X e Y das posições 0 e 1.
                if (parts.length >= 2) {
                    try {
                        double x, y, z = 0;

                        // Lógica inteligente de detecção de colunas
                        if (parts.length == 2) {
                            // Formato: X Y (Sem ID)
                            x = Double.parseDouble(parts[0].replace(",", "."));
                            y = Double.parseDouble(parts[1].replace(",", "."));
                        } else {
                            // Formato: ID X Y ... (Com ID, ignoramos o ID original)
                            x = Double.parseDouble(parts[1].replace(",", "."));
                            y = Double.parseDouble(parts[2].replace(",", "."));

                            if (parts.length > 3) {
                                z = Double.parseDouble(parts[3].replace(",", "."));
                            }
                        }

                        // --- AQUI ESTÁ A MÁGICA ---
                        // Geramos o nome padronizado "PT-" + número
                        String name = "PT-" + sequence++;

                        String desc = "";
                        // Tenta pegar descrição se houver (coluna 5 em diante)
                        if (parts.length > 4) desc = parts[4];

                        points.add(new TopoPoint(name, x, y, z, desc, false));

                    } catch (NumberFormatException e) {
                        System.err.println("Linha ignorada (formato inválido): " + line);
                    }
                }
            }
        }
        return points;
    }
}