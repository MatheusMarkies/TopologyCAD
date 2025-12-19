package com.brasens.layout.components.CAD.Canvas;

import com.brasens.functions.HandleFunctions;
import com.brasens.model.objects.TopoObject;
import com.brasens.model.objects.TopoPoint;
import com.brasens.utilities.math.Vector2D;
import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter @Setter
public class CadCanvas extends Canvas {

    private final Affine trans = new Affine();
    private final HandleFunctions functions;
    private double lastMouseX, lastMouseY;

    private final List<TopoObject> objects = new ArrayList<>();
    private TopoObject activePolyline = null;

    private double globalOffsetX = 0;
    private double globalOffsetY = 0;

    public CadCanvas(HandleFunctions functions) {
        super(100, 100);
        this.functions = functions;

        this.setOnMousePressed(this::handleMousePressed);
        this.setOnMouseDragged(this::handleMouseDragged);
        this.setOnMouseReleased(this::handleMouseReleased);
        this.setOnScroll(this::handleScroll);

        this.setOnMouseMoved(e -> {
            functions.updateMousePosition(e.getX(), e.getY());
            functions.handleMouseMove(new Vector2D(e.getX(), e.getY()), this);
        });

        functions.setEdgePanTimer(new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (functions.isEdgePanEnabled()) {
                    functions.updateEdgePan(getWidth(), getHeight());

                    double dx = functions.getEdgeDx();
                    double dy = functions.getEdgeDy();

                    if (dx != 0 || dy != 0) {
                        trans.prependTranslation(dx, dy);
                        redraw();
                    }
                }
            }
        });
        functions.getEdgePanTimer().start();
    }

    private void updateGlobalOffsets() {
        if (!objects.isEmpty() && !objects.get(0).getPoints().isEmpty()) {
            TopoPoint p = objects.get(0).getPoints().get(0);
            this.globalOffsetX = p.getX();
            this.globalOffsetY = p.getY();
        } else {
            this.globalOffsetX = 0;
            this.globalOffsetY = 0;
        }
    }

    public void setObjects(List<TopoObject> newObjects) {
        this.objects.clear();
        this.objects.addAll(newObjects);

        updateGlobalOffsets();

        zoomExtents();

        redraw();
    }

    public void addTextObject(double x, double y, String textContent) {
        TopoObject textObj = new TopoObject();
        textObj.setId("TXT-" + System.currentTimeMillis());
        textObj.setLayerName("TEXT");
        textObj.setClosed(false);
        textObj.addPoint(new TopoPoint(textContent, x, y));
        this.objects.add(textObj);

        if (objects.size() == 1) {
            updateGlobalOffsets();
            zoomExtents();
        }

        redraw();
    }

    public void setImportedPoints(List<TopoPoint> newPoints) {
        TopoObject perimetro = new TopoObject(newPoints, true);

        perimetro.setId("PERIMETRO");
        perimetro.setLayerName("PERIMETRO");

        this.objects.add(perimetro);

        if (objects.size() == 1) {
            updateGlobalOffsets();
        }

        zoomExtents();
        redraw();
    }

    public String getNextPointName(String... temporaryExcludedNames) {
        Set<Integer> usedIds = new HashSet<>();

        for (TopoObject obj : objects) {
            for (TopoPoint p : obj.getPoints()) {
                extractAndAddId(p.getName(), usedIds);
            }
        }

        if (temporaryExcludedNames != null) {
            for (String name : temporaryExcludedNames) {
                if (name != null) extractAndAddId(name, usedIds);
            }
        }

        int nextId = 0;
        while (usedIds.contains(nextId)) {
            nextId++;
        }

        return "PT-"+String.valueOf(nextId);
    }

    private void extractAndAddId(String name, Set<Integer> set) {
        if (name == null || name.isEmpty()) return;
        try {
            set.add(Integer.parseInt(name));
        } catch (NumberFormatException e) {
            Pattern p = Pattern.compile("\\d+");
            Matcher m = p.matcher(name);
            if (m.find()) {
                try {
                    set.add(Integer.parseInt(m.group()));
                } catch (Exception ignored) {}
            }
        }
    }

    public void addLineObject(TopoPoint start, TopoPoint end) {
        TopoObject line = new TopoObject();
        line.addPoint(start);
        line.addPoint(end);
        line.setClosed(false);

        this.objects.add(line);
        redraw();
    }

    public void createPolyLine(TopoPoint startPoint) {
        activePolyline = new TopoObject();

        activePolyline.setId("PLINE-NEW");
        activePolyline.setClosed(false);

        activePolyline.addPoint(startPoint);

        this.objects.add(activePolyline);

        redraw();
    }

    public void addPointToPolyLine(TopoPoint point) {
        if (activePolyline == null) return;

        List<TopoPoint> pts = activePolyline.getPoints();

        if (!pts.isEmpty()) {
            TopoPoint startPoint = pts.get(0);

            double dist = Math.hypot(point.getX() - startPoint.getX(), point.getY() - startPoint.getY());

            double snapTolerance = 15.0 / trans.getMxx();

            if (dist < snapTolerance) {
                System.out.println("Snap no ponto inicial! Fechando e parando comando...");

                activePolyline.setClosed(true);

                activePolyline = null;

                functions.setTempStartPoint(null);
                functions.setTempEndPoint(null);

                functions.setFunction(HandleFunctions.FunctionType.NONE);

                redraw();
                return;
            }
        }
        activePolyline.addPoint(point);
        redraw();
    }

    public void finishPolyLine() {
        if (activePolyline != null) {
            removeDuplicateLastPoint();
            checkGeometricClosure();
            finishPolyLineLogic();
        }
    }

    private void finishPolyLineLogic() {
        activePolyline = null;
        System.out.println("Polilinha finalizada.");

        functions.setTempStartPoint(null);
        functions.setTempEndPoint(null);

        redraw();
    }

    private void removeDuplicateLastPoint() {
        List<TopoPoint> pts = activePolyline.getPoints();
        if (pts.size() > 1) {
            TopoPoint last = pts.get(pts.size() - 1);
            TopoPoint penult = pts.get(pts.size() - 2);
            if (isSamePoint(last, penult)) {
                pts.remove(pts.size() - 1);
            }
        }
    }

    private void checkGeometricClosure() {
        List<TopoPoint> pts = activePolyline.getPoints();
        if (pts.size() >= 3) {
            TopoPoint first = pts.get(0);
            TopoPoint last = pts.get(pts.size() - 1);

            // Tolerância matemática fina (1mm) apenas para validação de dados
            if (Math.abs(first.getX() - last.getX()) < 0.001 &&
                    Math.abs(first.getY() - last.getY()) < 0.001) {

                activePolyline.setClosed(true);
                pts.remove(pts.size() - 1); // Remove o ponto duplicado se existir
            }
        }
    }

    // Método auxiliar para comparar coordenadas com segurança
    private boolean isSamePoint(TopoPoint p1, TopoPoint p2) {
        double tolerance = 0.001; // 1 milímetro de tolerância (ajuste se necessário)
        return Math.abs(p1.getX() - p2.getX()) < tolerance &&
                Math.abs(p1.getY() - p2.getY()) < tolerance;
    }

    public void redraw() {
        GraphicsContext gc = getGraphicsContext2D();

        // 1. Limpa Tela
        gc.setTransform(new Affine());
        gc.setFill(Color.rgb(30, 30, 30));
        gc.fillRect(0, 0, getWidth(), getHeight());

        // 2. Aplica a matriz
        gc.setTransform(trans);

        drawGrid(gc);

        double scale = trans.getMxx();
        double lineWidth = 1.5 / scale;
        double pointSize = 5 / scale;
        double fontSize = 12 / scale;
        double textOffset = 8 / scale;

        gc.setLineWidth(lineWidth);

        // --- CAMADA 1: OBJETOS (LINHAS) ---
        for (TopoObject obj : objects) {
            // Se for camada de TEXTO, não desenhamos linhas, pulamos para a próxima
            if ("TEXT".equals(obj.getLayerName())) continue;

            List<TopoPoint> pts = obj.getPoints();
            if (pts.size() < 2) continue;

            gc.setStroke(Color.CYAN);
            gc.beginPath();

            TopoPoint p0 = pts.get(0);
            gc.moveTo(p0.getX() - globalOffsetX, -(p0.getY() - globalOffsetY));

            for (int i = 1; i < pts.size(); i++) {
                TopoPoint p = pts.get(i);
                gc.lineTo(p.getX() - globalOffsetX, -(p.getY() - globalOffsetY));
            }

            if (obj.isClosed()) gc.closePath();
            gc.stroke();
        }

        // --- CAMADA 2: PREVIEW DA FERRAMENTA ---
        if ((functions.getFunctionSelected() == HandleFunctions.FunctionType.LINE
                || functions.getFunctionSelected() == HandleFunctions.FunctionType.POLYLINE)
                && functions.getTempStartPoint() != null
                && functions.getTempEndPoint() != null) {

            gc.setStroke(Color.WHITE);
            gc.setLineWidth(1.0 / scale);
            gc.setLineDashes(5 / scale);

            TopoPoint start = functions.getTempStartPoint();
            TopoPoint end = functions.getTempEndPoint();

            gc.strokeLine(
                    start.getX() - globalOffsetX, -(start.getY() - globalOffsetY),
                    end.getX() - globalOffsetX, -(end.getY() - globalOffsetY)
            );
            gc.setLineDashes(null);
        }

        // --- CAMADA 3: VÉRTICES E TEXTOS (LABEL) ---
        gc.setFont(javafx.scene.text.Font.font("Arial", fontSize));

        for (TopoObject obj : objects) {
            boolean isTextLayer = "TEXT".equals(obj.getLayerName());

            for (TopoPoint p : obj.getPoints()) {
                // Coordenadas relativas
                double drawX = p.getX() - globalOffsetX;
                double drawY = -(p.getY() - globalOffsetY);

                // --- LÓGICA DIFERENCIADA ---
                if (isTextLayer) {
                    // MODO TEXTO (LABEL)

                    // Se estiver selecionado, desenha uma caixinha ou cor diferente para indicar
                    if (p.isSelected()) {
                        gc.setFill(Color.ORANGERED);
                    } else {
                        gc.setFill(Color.WHITE);
                    }

                    // Desenha APENAS o texto (Nome do ponto guarda o conteúdo)
                    if (p.getName() != null) {
                        gc.fillText(p.getName(), drawX, drawY);
                    }

                    // Opcional: Desenha um pequeno "x" ou cruz apenas se estiver selecionado
                    // para saber onde é o ponto de ancoragem
                    if (p.isSelected()) {
                        double crossSize = 3 / scale;
                        gc.setStroke(Color.ORANGERED);
                        gc.setLineWidth(1 / scale);
                        gc.strokeLine(drawX - crossSize, drawY - crossSize, drawX + crossSize, drawY + crossSize);
                        gc.strokeLine(drawX - crossSize, drawY + crossSize, drawX + crossSize, drawY - crossSize);
                    }

                } else {
                    // MODO PONTO GEOMÉTRICO (BOLINHA + NOME)
                    if (p.isSelected()) {
                        gc.setFill(Color.ORANGERED);
                        double selSize = pointSize * 1.5;
                        gc.fillOval(drawX - selSize/2, drawY - selSize/2, selSize, selSize);
                    } else {
                        gc.setFill(Color.YELLOW);
                        gc.fillOval(drawX - pointSize/2, drawY - pointSize/2, pointSize, pointSize);
                    }

                    gc.setFill(Color.WHITE);
                    if (p.getName() != null && !p.getName().isEmpty()) {
                        gc.fillText(p.getName(), drawX + textOffset, drawY - textOffset);
                    }
                }
            }
        }
    }

    public Vector2D screenToWorld(double screenX, double screenY) {
        try {
            javafx.geometry.Point2D p = trans.inverseTransform(screenX, screenY);

            double worldX = p.getX() + globalOffsetX;
            double worldY = -p.getY() + globalOffsetY;

            return new Vector2D(worldX, worldY);
        } catch (NonInvertibleTransformException e) {
            return new Vector2D(0, 0);
        }
    }

    public Vector2D worldToScreen(double worldX, double worldY) {
        double relX = worldX - globalOffsetX;
        double relY = -(worldY - globalOffsetY);

        javafx.geometry.Point2D p = trans.transform(relX, relY);
        return new Vector2D(p.getX(), p.getY());
    }

    private void drawGrid(GraphicsContext gc) {
        double width = getWidth();
        double height = getHeight();
        double scale = trans.getMxx();

        try {
            // 1. Limites VISÍVEIS em Coordenadas RELATIVAS (baseado na câmera atual)
            javafx.geometry.Point2D p1 = trans.inverseTransform(0, 0);
            javafx.geometry.Point2D p2 = trans.inverseTransform(width, height);

            double minX_rel = Math.min(p1.getX(), p2.getX());
            double maxX_rel = Math.max(p1.getX(), p2.getX());
            double minY_rel = Math.min(p1.getY(), p2.getY());
            double maxY_rel = Math.max(p1.getY(), p2.getY());

            // 2. Converte para ABSOLUTAS (Mundo Real) para calcular onde as linhas caem (ex: 700.100, 700.200)
            double minX_abs = minX_rel + globalOffsetX;
            double maxX_abs = maxX_rel + globalOffsetX;
            // Lembra que Y é invertido: Y_abs = -Y_rel + OffsetY.
            // Mas para o grid, só queremos saber onde começam as linhas.
            // Vamos simplificar e trabalhar com o offset visual direto no loop.

            double targetPixelSpacing = 100.0;
            double rawStep = targetPixelSpacing / scale;
            double step = calculateNiceStep(rawStep);

            // Estilos
            double fontSize = 12 / scale;
            gc.setFont(javafx.scene.text.Font.font("Arial", fontSize));
            gc.setFill(Color.GRAY);
            gc.setStroke(Color.rgb(100, 100, 100, 0.2));
            gc.setLineWidth(1 / scale);

            double textOffset = 4 / scale;

            // 3. Grid VERTICAL (Varia X)
            // Começa no múltiplo de 'step' mais próximo dentro da visão ABSOLUTA
            double startX_abs = Math.floor(minX_abs / step) * step;

            for (double x_abs = startX_abs; x_abs <= maxX_abs; x_abs += step) {
                // VOLTA para Relativo para desenhar
                double drawX = x_abs - globalOffsetX;

                // Desenha a linha vertical cobrindo toda a altura visível relativa
                gc.strokeLine(drawX, minY_rel, drawX, maxY_rel);

                // Texto (Usa coordenada absoluta)
                String label = String.format("E %.0f", x_abs);
                gc.fillText(label, drawX + textOffset, maxY_rel - (textOffset * 2));
            }

            // 4. Grid HORIZONTAL (Varia Y)
            // Aqui é chato por causa do Y invertido.
            // minY_rel (topo visual, ex: -500) corresponde a uma coordenada Y_abs MAIOR (ex: 7.000.500)
            // maxY_rel (fundo visual, ex: 500) corresponde a uma coordenada Y_abs MENOR (ex: 6.999.500)

            // Calculamos Y Absoluto dos limites
            double y_abs_start = (-minY_rel) + globalOffsetY;
            double y_abs_end = (-maxY_rel) + globalOffsetY;

            double min_y_abs = Math.min(y_abs_start, y_abs_end);
            double max_y_abs = Math.max(y_abs_start, y_abs_end);

            double startY_abs = Math.floor(min_y_abs / step) * step;

            for (double y_abs = startY_abs; y_abs <= max_y_abs; y_abs += step) {
                // VOLTA para Relativo para desenhar: relY = -(absY - OffsetY)
                double drawY = -(y_abs - globalOffsetY);

                gc.strokeLine(minX_rel, drawY, maxX_rel, drawY);

                String label = String.format("N %.0f", y_abs);
                gc.fillText(label, minX_rel + textOffset, drawY - textOffset);
            }

        } catch (javafx.scene.transform.NonInvertibleTransformException e) {
            e.printStackTrace();
        }
    }

    public TopoPoint findPointNear(double screenX, double screenY, double toleranceInPixels) {

        for (TopoObject obj : objects) {
            for (TopoPoint p : obj.getPoints()) {

                Vector2D screenPos = worldToScreen(p.getX(), p.getY());

                double dx = screenPos.x() - screenX;
                double dy = screenPos.y() - screenY;
                double dist = Math.sqrt(dx*dx + dy*dy);

                if (dist < toleranceInPixels) {
                    return p;
                }
            }
        }
        return null; // Nenhum ponto encontrado perto
    }

    public void clearSelection() {
        for (TopoObject obj : objects) {
            for (TopoPoint p : obj.getPoints()) {
                p.setSelected(false);
            }
        }
        redraw();
    }

    private double calculateNiceStep(double rawStep) {
        double exponent = Math.floor(Math.log10(rawStep));
        double fraction = rawStep / Math.pow(10, exponent);

        double niceFraction;
        if (fraction < 2.0) niceFraction = 1.0;
        else if (fraction < 5.0) niceFraction = 2.0;
        else niceFraction = 5.0;

        return niceFraction * Math.pow(10, exponent);
    }

    public void zoomExtents() {
        if (objects.isEmpty()) return;

        double minX = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;
        boolean hasPoints = false;

        for (TopoObject obj : objects) {
            for (TopoPoint p : obj.getPoints()) {
                hasPoints = true;

                double pY = p.getY();
                if (p.getX() < minX) minX = p.getX();
                if (p.getX() > maxX) maxX = p.getX();
                if (pY < minY) minY = pY;
                if (pY > maxY) maxY = pY;
            }
        }

        if (!hasPoints) return;

        double dataWidth = maxX - minX;
        double dataHeight = maxY - minY;

        double margin = Math.max(dataWidth, dataHeight) * 0.1;
        if (dataWidth == 0) dataWidth = 10;
        if (dataHeight == 0) dataHeight = 10;

        double scaleX = getWidth() / (dataWidth + margin * 2);
        double scaleY = getHeight() / (dataHeight + margin * 2);
        double scale = Math.min(scaleX, scaleY);

        if (Double.isNaN(scale) || Double.isInfinite(scale) || scale == 0) scale = 1.0;

        double centerX = (minX + maxX) / 2;
        double centerY = (minY + maxY) / 2;

        double relCenterX = centerX - globalOffsetX;
        double relCenterY = -(centerY - globalOffsetY);

        trans.setToIdentity();
        trans.appendTranslation(getWidth() / 2, getHeight() / 2);
        trans.appendScale(scale, scale);
        trans.appendTranslation(-relCenterX, -relCenterY);

        redraw();
    }

    long clickTime = 0;

    private void handleMousePressed(MouseEvent e) {
        lastMouseX = e.getX();
        lastMouseY = e.getY();

        functions.updateMousePosition(e.getX(), e.getY());
        functions.startDrag();

        double dTime = (System.nanoTime() - clickTime) / 1e6;

        clickTime = System.nanoTime();

        if (dTime < 250) {
            functions.handleDoubleClick(new Vector2D(lastMouseX, lastMouseY), this);
        }
        else {
            functions.handleClick(new Vector2D(lastMouseX, lastMouseY), this);
        }
    }

    private void handleMouseReleased(MouseEvent e) {
        functions.stopDrag();
    }

    private void handleMouseDragged(MouseEvent e) {
        functions.updateMousePosition(e.getX(), e.getY());

        if (e.getButton() == MouseButton.MIDDLE || e.getButton() == MouseButton.PRIMARY) {
            double dx = e.getX() - lastMouseX;
            double dy = e.getY() - lastMouseY;

            try {
                trans.prependTranslation(dx, dy);
                redraw();
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            lastMouseX = e.getX();
            lastMouseY = e.getY();
        }
    }

    private void handleScroll(ScrollEvent e) {
        functions.handleScroll(e, this);

        double zoomFactor = 1.1;
        if (e.getDeltaY() < 0) {
            zoomFactor = 1 / zoomFactor;
        }

        double pivotX = e.getX();
        double pivotY = e.getY();

        try {
            javafx.geometry.Point2D worldPoint = trans.inverseTransform(pivotX, pivotY);
            trans.appendScale(zoomFactor, zoomFactor, worldPoint.getX(), worldPoint.getY());
            redraw();
        } catch (NonInvertibleTransformException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public boolean isResizable() {
        return true;
    }

    @Override
    public double prefWidth(double height) {
        return getWidth();
    }

    @Override
    public double prefHeight(double width) {
        return getHeight();
    }
}