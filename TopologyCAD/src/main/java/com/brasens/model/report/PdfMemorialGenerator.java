package com.brasens.model.report;

import com.brasens.model.objects.TopoObject;
import com.brasens.model.objects.TopoPoint;
import com.brasens.utilities.math.TopologyMath;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;

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
    private static final Font FONT_BOLD = FontFactory.getFont(FontFactory.HELVETICA, 11, Font.BOLD);
    private static final Font FONT_NORMAL = FontFactory.getFont(FontFactory.HELVETICA, 11, Font.NORMAL);
    private static final Font FONT_SMALL = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.NORMAL);

    public static void generateAndSave(TopoObject poly, ProjectData data, File file) throws IOException, DocumentException {
        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
        PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();

        // 1. TÍTULO
        Paragraph title = new Paragraph("MEMORIAL DESCRITIVO", FONT_TITLE);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        // 2. DADOS DO IMÓVEL (Tabela invisível para alinhar)
        addMetadata(document, "IMÓVEL:", data.getPropertyInfo().getNomePropriedade());
        addMetadata(document, "PROPRIETÁRIO:", data.getPropertyInfo().getProprietario());
        addMetadata(document, "MUNICÍPIO:", data.getPropertyInfo().getMunicipio());
        addMetadata(document, "ÁREA:", String.format("%.4f ha", poly.getAreaHa()));
        addMetadata(document, "PERÍMETRO:", String.format("%.2f m", poly.getPerimeter()));

        document.add(new Paragraph("\n")); // Espaço

        // 3. TEXTO DESCRITIVO
        Paragraph descHeader = new Paragraph("DESCRIÇÃO DO PERÍMETRO:", FONT_BOLD);
        descHeader.setSpacingAfter(10);
        document.add(descHeader);

        Paragraph body = new Paragraph();
        body.setAlignment(Element.ALIGN_JUSTIFIED);
        body.setLeading(18f); // Espaçamento entre linhas (1.5x)

        List<TopoPoint> points = poly.getPoints();
        int n = points.size();
        TopoPoint startP = points.get(0);

        // Início
        body.add(new Chunk("Inicia-se a descrição deste perímetro no vértice ", FONT_NORMAL));
        body.add(new Chunk(startP.getName(), FONT_BOLD));
        body.add(new Chunk(", de coordenadas N=" + dfCoord.format(startP.getY()) + "m e E=" + dfCoord.format(startP.getX()) + "m; ", FONT_NORMAL));

        // Loop dos segmentos
        for (int i = 0; i < n; i++) {
            TopoPoint current = points.get(i);
            TopoPoint next = points.get((i + 1) % n);

            double dist = TopologyMath.getDistance2D(current, next);
            double azimute = TopologyMath.getAzimuth(current, next);
            String azimuteStr = TopologyMath.degreesToDMS(azimute);

            String confrontante = poly.getConfrontante(i);
            if (confrontante == null || confrontante.isEmpty()) confrontante = "quem de direito";

            body.add(new Chunk("Deste, segue confrontando com ", FONT_NORMAL));
            body.add(new Chunk(confrontante, FONT_BOLD));
            body.add(new Chunk(", com o azimute de " + azimuteStr + " e distância de " + dfDist.format(dist) + "m até o vértice ", FONT_NORMAL));
            body.add(new Chunk(next.getName(), FONT_BOLD));

            if (i == n - 1) {
                // Último ponto (Fechamento)
                body.add(new Chunk(", de coordenadas N=" + dfCoord.format(next.getY()) + "m e E=" + dfCoord.format(next.getX()) + "m. ", FONT_NORMAL));
            } else {
                body.add(new Chunk(", de coordenadas N=" + dfCoord.format(next.getY()) + "m e E=" + dfCoord.format(next.getX()) + "m; ", FONT_NORMAL));
            }
        }

        body.add(new Chunk("Fechando assim o perímetro acima descrito.", FONT_NORMAL));
        document.add(body);

        // 4. ASSINATURA
        document.add(new Paragraph("\n\n\n\n"));
        Paragraph dateLine = new Paragraph(data.getPropertyInfo().getMunicipio() + ", ______ de __________________ de 20____.", FONT_NORMAL);
        dateLine.setAlignment(Element.ALIGN_CENTER);
        document.add(dateLine);

        document.add(new Paragraph("\n\n"));

        Paragraph lineSign = new Paragraph("___________________________________________", FONT_NORMAL);
        lineSign.setAlignment(Element.ALIGN_CENTER);
        document.add(lineSign);

        Paragraph respName = new Paragraph(data.getProprietarioAssinatura().getNome(), FONT_BOLD);
        respName.setAlignment(Element.ALIGN_CENTER);
        document.add(respName);

        Paragraph respCred = new Paragraph(data.getProprietarioAssinatura().getRegistroClass(), FONT_SMALL);
        respCred.setAlignment(Element.ALIGN_CENTER);
        document.add(respCred);

        document.close();
    }

    private static void addMetadata(Document doc, String label, String value) throws DocumentException {
        Paragraph p = new Paragraph();
        p.add(new Chunk(label + " ", FONT_BOLD));

        String safeValue = (value == null) ? "" : value;

        p.add(new Chunk(safeValue, FONT_NORMAL));
        doc.add(p);
    }
}