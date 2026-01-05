package com.brasens.model.report;

import com.brasens.model.objects.TopoObject;
import com.brasens.model.objects.TopoPoint;
import com.brasens.utilities.math.TopologyMath;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

public class PdfMemorialGenerator {

    private static final DecimalFormat dfDist = new DecimalFormat("0.00");
    private static final DecimalFormat dfCoord = new DecimalFormat("0.000");

    // Fontes
    private static final Font FONT_TITLE = FontFactory.getFont(FontFactory.HELVETICA, 14, Font.BOLD);
    private static final Font FONT_BOLD = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.BOLD);
    private static final Font FONT_NORMAL = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.NORMAL);
    private static final Font FONT_SMALL = FontFactory.getFont(FontFactory.HELVETICA, 8, Font.NORMAL);
    private static final Font FONT_TABLE_HEADER = FontFactory.getFont(FontFactory.HELVETICA, 9, Font.BOLD, Color.WHITE);

    public static void generateAndSave(TopoObject poly, ProjectData data, File file) throws IOException, DocumentException {
        Document document = new Document(PageSize.A4, 40, 40, 40, 40);
        PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();

        // 1. TÍTULO
        Paragraph title = new Paragraph("MEMORIAL DESCRITIVO", FONT_TITLE);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        // 2. DADOS DO IMÓVEL
        addMetadata(document, "IMÓVEL:", data.getPropertyInfo().getNomePropriedade());
        addMetadata(document, "PROPRIETÁRIO:", data.getPropertyInfo().getProprietario());
        addMetadata(document, "MUNICÍPIO:", data.getPropertyInfo().getMunicipio());
        addMetadata(document, "ÁREA:", String.format("%.4f ha", poly.getAreaHa()));
        addMetadata(document, "PERÍMETRO:", String.format("%.2f m", poly.getPerimeter()));

        document.add(new Paragraph("\n"));

        // 3. TEXTO DESCRITIVO
        Paragraph descHeader = new Paragraph("DESCRIÇÃO DO PERÍMETRO:", FONT_BOLD);
        descHeader.setSpacingAfter(10);
        document.add(descHeader);

        Paragraph body = new Paragraph();
        body.setAlignment(Element.ALIGN_JUSTIFIED);
        body.setLeading(16f);

        List<TopoPoint> points = poly.getPoints();
        int n = points.size();
        TopoPoint startP = points.get(0);

        body.add(new Chunk("Inicia-se a descrição deste perímetro no vértice ", FONT_NORMAL));
        body.add(new Chunk(safeName(startP.getName()), FONT_BOLD));
        body.add(new Chunk(", de coordenadas N=" + dfCoord.format(startP.getY()) + "m e E=" + dfCoord.format(startP.getX()) + "m; ", FONT_NORMAL));

        for (int i = 0; i < n; i++) {
            TopoPoint current = points.get(i);
            TopoPoint next = points.get((i + 1) % n);

            // Uso correto dos métodos da TopologyMath existente
            double dist = TopologyMath.getDistance2D(current, next);
            double azimute = TopologyMath.getAzimuth(current, next);
            String azimuteStr = TopologyMath.degreesToDMS(azimute);

            String confrontante = poly.getConfrontante(i);
            if (confrontante == null || confrontante.isEmpty()) confrontante = "quem de direito";

            body.add(new Chunk("Deste, segue confrontando com ", FONT_NORMAL));
            body.add(new Chunk(confrontante, FONT_BOLD));
            body.add(new Chunk(", com o azimute de " + azimuteStr + " e distância de " + dfDist.format(dist) + "m até o vértice ", FONT_NORMAL));
            body.add(new Chunk(safeName(next.getName()), FONT_BOLD));

            if (i == n - 1) {
                body.add(new Chunk(", de coordenadas N=" + dfCoord.format(next.getY()) + "m e E=" + dfCoord.format(next.getX()) + "m. ", FONT_NORMAL));
            } else {
                body.add(new Chunk(", de coordenadas N=" + dfCoord.format(next.getY()) + "m e E=" + dfCoord.format(next.getX()) + "m; ", FONT_NORMAL));
            }
        }

        body.add(new Chunk("Fechando assim o perímetro acima descrito.", FONT_NORMAL));
        document.add(body);

        // 4. QUADRO DE COORDENADAS (Anexo Técnico)
        document.newPage();
        Paragraph tableHeader = new Paragraph("QUADRO DE COORDENADAS", FONT_TITLE);
        tableHeader.setAlignment(Element.ALIGN_CENTER);
        tableHeader.setSpacingAfter(10);
        document.add(tableHeader);

        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1.5f, 2f, 1.5f, 2.5f, 2.5f, 3f});

        addHeaderCell(table, "Vértice");
        addHeaderCell(table, "Azimute");
        addHeaderCell(table, "Dist (m)");
        addHeaderCell(table, "Norte (Y)");
        addHeaderCell(table, "Leste (X)");
        addHeaderCell(table, "Confrontante");

        for (int i = 0; i < n; i++) {
            TopoPoint current = points.get(i);
            TopoPoint next = points.get((i + 1) % n);

            double dist = TopologyMath.getDistance2D(current, next);
            double azimute = TopologyMath.getAzimuth(current, next);
            String confrontante = poly.getConfrontante(i);
            if(confrontante == null) confrontante = "-";

            addCell(table, safeName(current.getName()));
            addCell(table, TopologyMath.degreesToDMS(azimute));
            addCell(table, dfDist.format(dist));
            addCell(table, dfCoord.format(current.getY()));
            addCell(table, dfCoord.format(current.getX()));
            addCell(table, confrontante);
        }
        document.add(table);

        // 5. ASSINATURAS
        document.add(new Paragraph("\n\n\n"));
        Paragraph dateLine = new Paragraph(safeStr(data.getPropertyInfo().getMunicipio()) + ", ______ de __________________ de 20____.", FONT_NORMAL);
        dateLine.setAlignment(Element.ALIGN_CENTER);
        document.add(dateLine);

        document.add(new Paragraph("\n\n"));
        Paragraph lineSign = new Paragraph("___________________________________________", FONT_NORMAL);
        lineSign.setAlignment(Element.ALIGN_CENTER);
        document.add(lineSign);

        Paragraph respName = new Paragraph(safeStr(data.getProprietarioAssinatura().getNome()), FONT_BOLD);
        respName.setAlignment(Element.ALIGN_CENTER);
        document.add(respName);

        Paragraph respCred = new Paragraph(safeStr(data.getProprietarioAssinatura().getRegistroClass()), FONT_SMALL);
        respCred.setAlignment(Element.ALIGN_CENTER);
        document.add(respCred);

        document.close();
    }

    // --- Auxiliares Seguros ---

    private static void addMetadata(Document doc, String label, String value) throws DocumentException {
        Paragraph p = new Paragraph();
        p.add(new Chunk(label + " ", FONT_BOLD));
        p.add(new Chunk(safeStr(value), FONT_NORMAL));
        doc.add(p);
    }

    private static void addHeaderCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FONT_TABLE_HEADER));
        cell.setBackgroundColor(Color.DARK_GRAY);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(4);
        table.addCell(cell);
    }

    private static void addCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FONT_SMALL));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    // Evita NullPointerException se os dados não estiverem preenchidos
    private static String safeStr(String s) {
        return (s == null) ? "" : s;
    }

    private static String safeName(String s) {
        return (s == null || s.isEmpty()) ? "P" : s;
    }
}