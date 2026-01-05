package com.brasens.model.io;

import com.brasens.model.objects.TopoObject;
import com.brasens.model.objects.TopoPoint;
import com.brasens.model.objects.TopoTableObject;
import com.brasens.model.report.ProjectData;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.*;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class PdfMapPlotter {

    // Margens e Layout (A3 Paisagem: 842 x 595 pontos)
    private static final float PAGE_WIDTH = PageSize.A3.rotate().getWidth();
    private static final float PAGE_HEIGHT = PageSize.A3.rotate().getHeight();
    private static final float MARGIN = 20;
    private static final float SIDEBAR_WIDTH = 200; // Largura da barra lateral
    private static final float MAP_WIDTH = PAGE_WIDTH - SIDEBAR_WIDTH - (MARGIN * 2);
    private static final float MAP_HEIGHT = PAGE_HEIGHT - (MARGIN * 2);

    public static void export(List<TopoObject> objects, ProjectData data, File file) throws IOException, DocumentException {
        Document document = new Document(PageSize.A3.rotate(), MARGIN, MARGIN, MARGIN, MARGIN);
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();

        PdfContentByte cb = writer.getDirectContent();

        // 1. Desenhar Estrutura (Borda e Barra Lateral)
        drawLayout(cb, data);

        // 2. Calcular Escala e Limites do Desenho
        double[] bounds = calculateBounds(objects); // minX, minY, maxX, maxY
        double scale = calculateScale(bounds);

        // Centralizar o desenho na área do mapa
        double drawingWidth = (bounds[2] - bounds[0]) * scale;
        double drawingHeight = (bounds[3] - bounds[1]) * scale;
        double offsetX = MARGIN + (MAP_WIDTH - drawingWidth) / 2.0;
        double offsetY = MARGIN + (MAP_HEIGHT - drawingHeight) / 2.0;

        // 3. Desenhar Objetos do Canvas (Linhas, Polígonos e Tabelas)
        drawMapObjects(cb, objects, bounds, scale, offsetX, offsetY);

        document.close();
    }

    private static void drawLayout(PdfContentByte cb, ProjectData data) throws DocumentException {
        // Borda Geral
        cb.setLineWidth(1f);
        cb.setColorStroke(Color.BLACK);
        cb.rectangle(MARGIN, MARGIN, PAGE_WIDTH - 2 * MARGIN, PAGE_HEIGHT - 2 * MARGIN);
        cb.stroke();

        // Linha Divisória da Barra Lateral
        float sidebarX = PAGE_WIDTH - MARGIN - SIDEBAR_WIDTH;
        cb.moveTo(sidebarX, MARGIN);
        cb.lineTo(sidebarX, PAGE_HEIGHT - MARGIN);
        cb.stroke();

        // --- Conteúdo da Barra Lateral ---
        ColumnText ct = new ColumnText(cb);
        // Define a área de texto da barra lateral (com padding)
        ct.setSimpleColumn(sidebarX + 10, MARGIN + 10, PAGE_WIDTH - MARGIN - 10, PAGE_HEIGHT - MARGIN - 10);

        // Adicionar Dados
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        Font textFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

        ct.addElement(new Paragraph("PROJETO TOPOGRÁFICO", titleFont));
        ct.addElement(new Paragraph("\n")); // Espaço

        // Imagem (Planta de Localização - Se houver)
        if (data.getPlantaLocalizacao() != null) {
            try {
                // Converte JavaFX Image para iText Image (Simplificado via Swing/AWT se necessário,
                // mas aqui assumimos que ImageUtils trataria ou pegamos o path se disponível.
                // Como ProjectData guarda JavaFX Image, idealmente salvaríamos temporariamente ou converteríamos buffer)
                // Para este exemplo, vamos pular a conversão complexa de BufferImage e focar nos textos.
                ct.addElement(new Paragraph("[Imagem de Localização]", textFont));
                ct.addElement(new Paragraph("\n"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Dados do Projeto
        addSideInfo(ct, "PROPRIEDADE:", data.getPropertyInfo().getNomePropriedade(), labelFont, textFont);
        addSideInfo(ct, "PROPRIETÁRIO:", data.getPropertyInfo().getProprietario(), labelFont, textFont);
        addSideInfo(ct, "MUNICÍPIO:", data.getPropertyInfo().getMunicipio(), labelFont, textFont);
        addSideInfo(ct, "RESP. TÉCNICO:", data.getProprietarioAssinatura().getNome(), labelFont, textFont);
        addSideInfo(ct, "REGISTRO:", data.getPropertyInfo().getMatricula(), labelFont, textFont);

        ct.addElement(new Paragraph("\n"));
        addSideInfo(ct, "DATUM:", data.getTechnicalSpecs().getDatum(), labelFont, textFont);
        addSideInfo(ct, "SISTEMA:", data.getTechnicalSpecs().getSistemaCoordenadas(), labelFont, textFont);

        ct.go();
    }

    private static void addSideInfo(ColumnText ct, String label, String value, Font labelFont, Font valFont) {
        Phrase p = new Phrase();
        p.add(new Chunk(label + " ", labelFont));
        p.add(new Chunk(value != null ? value : "-", valFont));
        ct.addElement(p);
    }

    private static void drawMapObjects(PdfContentByte cb, List<TopoObject> objects, double[] bounds, double scale, double offX, double offY) throws DocumentException, IOException {
        double minX = bounds[0];
        double minY = bounds[1]; // No mundo, Y sobe. No PDF, Y sobe.

        for (TopoObject obj : objects) {
            // Ignora objetos invisíveis ou de sistema
            if ("FOLHA".equals(obj.getLayerName()) || "GRID".equals(obj.getLayerName())) continue;

            // --- CASO 1: Tabela do Usuário (TopoTableObject) ---
            if (obj instanceof TopoTableObject) {
                drawUserTable(cb, (TopoTableObject) obj, minX, minY, scale, offX, offY);
                continue;
            }

            // --- CASO 2: Geometria (Linhas/Polígonos) ---
            if (obj.getPoints().size() > 1) {
                cb.setLineWidth(1f);

                // Cor baseada na seleção ou layer (Padrão Preto para plotagem técnica)
                cb.setColorStroke(Color.BLACK);
                if ("PERIMETRO".equals(obj.getLayerName())) {
                    cb.setLineWidth(1.5f); // Mais grosso
                    cb.setColorStroke(Color.RED);
                }

                List<TopoPoint> pts = obj.getPoints();
                double startX = offX + (pts.get(0).getX() - minX) * scale;
                double startY = offY + (pts.get(0).getY() - minY) * scale;

                cb.moveTo((float) startX, (float) startY);

                for (int i = 1; i < pts.size(); i++) {
                    double px = offX + (pts.get(i).getX() - minX) * scale;
                    double py = offY + (pts.get(i).getY() - minY) * scale;
                    cb.lineTo((float) px, (float) py);
                }

                if (obj.isClosed()) {
                    cb.closePath();
                }
                cb.stroke();
            }

            // --- CASO 3: Textos (Cotas e Nomes) ---
            if ("TEXT".equals(obj.getLayerName()) && !obj.getPoints().isEmpty()) {
                TopoPoint p = obj.getPoints().get(0);
                double tx = offX + (p.getX() - minX) * scale;
                double ty = offY + (p.getY() - minY) * scale;

                // Desenha texto
                BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
                cb.beginText();
                cb.setFontAndSize(bf, 8); // Tamanho fixo ou proporcional? Fixo 8pt é legível
                cb.setColorFill(Color.BLACK);
                cb.showTextAligned(Element.ALIGN_CENTER, p.getName(), (float) tx, (float) ty, 0);
                cb.endText();
            }
        }
    }

    private static void drawUserTable(PdfContentByte cb, TopoTableObject tableObj, double minX, double minY, double scale, double offX, double offY) {
        // A posição da tabela no mundo
        TopoPoint origin = tableObj.getPoints().get(0);

        // Converter para posição no PDF
        double pdfX = offX + (origin.getX() - minX) * scale;
        double pdfY = offY + (origin.getY() - minY) * scale;

        // Criar a tabela iText
        int numCols = tableObj.getHeaders().length;
        PdfPTable pdfTable = new PdfPTable(numCols);

        // Define largura total da tabela (ajustada pela escala ou fixa?)
        // Vamos tentar manter uma largura proporcional legível
        float tableWidthPts = (float) (tableObj.getTotalWidth() * scale);
        // Se ficar muito pequeno (zoom out), forçamos um mínimo?
        // Para desenho técnico, geralmente queremos que a tabela apareça onde ela foi desenhada.
        pdfTable.setTotalWidth(tableWidthPts);
        pdfTable.setLockedWidth(true);

        // Cabeçalhos
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.WHITE);
        for (String header : tableObj.getHeaders()) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setBackgroundColor(Color.DARK_GRAY);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(3);
            pdfTable.addCell(cell);
        }

        // Dados
        Font dataFont = FontFactory.getFont(FontFactory.HELVETICA, 8);
        for (String[] row : tableObj.getDataRows()) {
            for (String cellData : row) {
                PdfPCell cell = new PdfPCell(new Phrase(cellData != null ? cellData : "", dataFont));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(2);
                pdfTable.addCell(cell);
            }
        }

        // Escrever a tabela no Canvas do PDF na posição absoluta
        // Nota: writeSelectedRows desenha de cima para baixo.
        // pdfY no nosso cálculo é a base (TopoTableObject origin geralmente é Top-Left no CAD visual logic?
        // No TopoTableObject.java não especifica, mas vamos assumir Top-Left visual).
        // Se a origem for top-left, pdfY é o topo.

        // Ajuste Y: O writeSelectedRows usa coordenada Y do TOPO da tabela.
        // Como o eixo Y do PDF sobe, precisamos garantir que desenhamos no lugar certo.

        pdfTable.writeSelectedRows(0, -1, (float) pdfX, (float) pdfY, cb);
    }

    private static double[] calculateBounds(List<TopoObject> objects) {
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE;
        boolean found = false;

        for (TopoObject obj : objects) {
            if ("FOLHA".equals(obj.getLayerName())) continue; // Ignora a folha CAD para calcular o zoom do PDF
            for (TopoPoint p : obj.getPoints()) {
                if (p.getX() < minX) minX = p.getX();
                if (p.getX() > maxX) maxX = p.getX();
                if (p.getY() < minY) minY = p.getY();
                if (p.getY() > maxY) maxY = p.getY();
                found = true;
            }
        }

        if (!found) return new double[]{0, 0, 100, 100};
        return new double[]{minX, minY, maxX, maxY};
    }

    private static double calculateScale(double[] bounds) {
        double dataW = bounds[2] - bounds[0];
        double dataH = bounds[3] - bounds[1];

        if (dataW == 0) dataW = 1;
        if (dataH == 0) dataH = 1;

        // Tenta encaixar no espaço do mapa (MAP_WIDTH x MAP_HEIGHT)
        double scaleX = MAP_WIDTH / dataW;
        double scaleY = MAP_HEIGHT / dataH;

        return Math.min(scaleX, scaleY) * 0.95; // 95% para margem de segurança
    }
}