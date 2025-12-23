package com.brasens.layout.components.CAD.Canvas;

import com.brasens.CAD;
import com.brasens.functions.HandleFunctions;
import com.brasens.model.TopoLineType;
import com.brasens.model.objects.TopoObject;
import com.brasens.model.objects.TopoPoint;
import com.brasens.utilities.math.Vector2D;
import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.FontWeight;
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter @Setter
public class CadCanvas extends Canvas {

    private final Affine trans = new Affine();
    private final HandleFunctions functions;
    private double lastMouseX, lastMouseY;

    private final List<TopoObject> objects = new ArrayList<>();
    private TopoObject activePolyline = null;

    @Setter
    private Runnable onContentChange;

    @Setter
    private Consumer<TopoPoint> onSelectionChanged;

    private double globalOffsetX = 0;
    private double globalOffsetY = 0;

    private double startDragX, startDragY;
    private boolean isDraggingConfirmed = false;
    private final double DRAG_THRESHOLD = 5.0;

    private ContextMenu contextMenu;

    private Image compassImage;
    private final double COMPASS_SIZE = 80.0;
    private final double COMPASS_MARGIN = 25.0;

    private final Map<String, Boolean> layerVisibility = new HashMap<>();

    private boolean showBackgroundGrid = true;

    private static final List<TopoObject> CLIPBOARD = new ArrayList<>();

    private enum ResizeHandle { NONE, RIGHT, BOTTOM, CORNER }
    private ResizeHandle activeResizeHandle = ResizeHandle.NONE;
    private com.brasens.model.objects.TopoTableObject resizingTable = null;
    private Vector2D lastResizeMousePos = null;

    private Color crossingSelectionColor = Color.GREEN;
    private Color selectionColor = Color.ALICEBLUE;

    private Color crossingSelectionFillColor = Color.rgb(0, 255, 55, 0.2);
    private Color selectionFillColor = Color.rgb(0, 125, 255, 0.2);

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

        layerVisibility.put("DEFAULT", true);
        layerVisibility.put("TEXT", true);
        layerVisibility.put("CURVA_MESTRA", true);
        layerVisibility.put("CURVA_NORMAL", true);
        layerVisibility.put("TRIANGULACAO", false);
        layerVisibility.put("TABELA", true);
        layerVisibility.put("ROSA_VENTOS", true);

        try {
            compassImage = new Image(
                    CAD.class.getResourceAsStream("/mspm/icons/rosa-dos-ventos.png"),
                    COMPASS_SIZE, // Largura desejada
                    COMPASS_SIZE, // Altura desejada
                    true,         // preserveRatio
                    true          // smooth
            );
        } catch (Exception e) {
            System.err.println("Imagem da Rosa dos Ventos não encontrada.");
        }

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

            if (Math.abs(first.getX() - last.getX()) < 0.001 &&
                    Math.abs(first.getY() - last.getY()) < 0.001) {

                activePolyline.setClosed(true);
                pts.remove(pts.size() - 1);
            }
        }
    }

    private boolean isSamePoint(TopoPoint p1, TopoPoint p2) {
        double tolerance = 0.001;
        return Math.abs(p1.getX() - p2.getX()) < tolerance &&
                Math.abs(p1.getY() - p2.getY()) < tolerance;
    }

    public void setLayerVisible(String layerName, boolean visible) {
        layerVisibility.put(layerName, visible);
        redraw();
    }

    public boolean isLayerVisible(String layerName) {
        return layerVisibility.getOrDefault(layerName, true);
    }

    public void createTableAt(double x, double y) {
        List<TopoPoint> allPoints = getAllPoints();
        if (allPoints.isEmpty()) {
            System.out.println("Sem pontos para gerar tabela.");
            return;
        }

        double currentScale = trans.getMxx();
        if (currentScale == 0) currentScale = 1.0;

        double targetPixelSize = 12.0;

        double worldFontSize = targetPixelSize / currentScale;

        double worldRowHeight = worldFontSize * 2.0;
        double worldColWidth = worldFontSize * 6.0;

        com.brasens.model.objects.TopoTableObject table =
                new com.brasens.model.objects.TopoTableObject(x, y, new ArrayList<>(allPoints));

        table.setFontSize(worldFontSize);
        table.setRowHeight(worldRowHeight);
        table.setColWidth(worldColWidth);

        this.objects.add(table);
        redraw();
    }

    public void redraw() {
        GraphicsContext gc = getGraphicsContext2D();

        // 1. Limpeza e Fundo
        gc.setTransform(new Affine());
        gc.setFill(Color.rgb(30, 30, 30));
        gc.fillRect(0, 0, getWidth(), getHeight());

        // 2. Aplica Zoom/Pan
        gc.setTransform(trans);

        if (showBackgroundGrid) {
            drawGrid(gc);
        }

        double scale = trans.getMxx();
        double pointSize = 5 / scale;
        double fontSize = 12 / scale;
        double textOffset = 8 / scale;

        // =================================================================
        // LOOP 1: GEOMETRIA (LINHAS, POLILINHAS E TABELAS)
        // =================================================================
        for (TopoObject obj : objects) {
            if (!isLayerVisible(obj.getLayerName())) continue;

            // --- LÓGICA DE TABELA CORRIGIDA ---
            if (obj instanceof com.brasens.model.objects.TopoTableObject) {
                com.brasens.model.objects.TopoTableObject tbl = (com.brasens.model.objects.TopoTableObject) obj;

                // Desenha a estrutura da tabela
                drawTable(gc, tbl, scale);

                // SE ESTIVER SELECIONADA, DESENHA OS GRIPS DE RESIZE
                if (isObjectSelected(tbl)) {
                    drawResizeHandles(gc, tbl, scale);
                }

                continue; // Pula o resto do loop para não tentar desenhar linhas/pontos neste objeto
            }

            if ("TEXT".equals(obj.getLayerName())) continue;

            List<TopoPoint> pts = obj.getPoints();

            if (pts.size() < 2) continue;

            TopoLineType style = obj.getType();
            boolean isSelected = isObjectSelected(obj);

            // Estilo da linha
            if (isSelected) {
                gc.setStroke(Color.ORANGERED);
                gc.setLineWidth((style.getWidth() + 1.5) / scale);
            } else {
                gc.setStroke(style.getColor());
                gc.setLineWidth(style.getWidth() / scale);
            }

            // Tracejado
            if (style.getDashArray() != null) {
                double[] originalDashes = style.getDashArray();
                double[] scaledDashes = new double[originalDashes.length];
                for (int i = 0; i < originalDashes.length; i++) {
                    scaledDashes[i] = originalDashes[i] / scale;
                }
                gc.setLineDashes(scaledDashes);
            } else {
                gc.setLineDashes(null);
            }

            // Desenha o caminho (Path)
            gc.beginPath();
            TopoPoint p0 = pts.get(0);
            gc.moveTo(p0.getX() - globalOffsetX, -(p0.getY() - globalOffsetY));

            for (int i = 1; i < pts.size(); i++) {
                TopoPoint p = pts.get(i);
                gc.lineTo(p.getX() - globalOffsetX, -(p.getY() - globalOffsetY));
            }

            if (obj.isClosed()) gc.closePath();
            gc.stroke();
            gc.setLineDashes(null);

            // Rótulos de cota (Curva Mestra)
            if (style == TopoLineType.CURVA_MESTRA && pts.size() >= 2) {
                TopoPoint p1 = pts.get(0);
                TopoPoint p2 = pts.get(1);
                double midX = (p1.getX() + p2.getX()) / 2.0;
                double midY = (p1.getY() + p2.getY()) / 2.0;

                gc.setFill(style.getColor());
                gc.setFont(javafx.scene.text.Font.font("Arial", FontWeight.BOLD, fontSize));
                gc.fillText(String.format("%.0f", p1.getZ()), midX - globalOffsetX, -(midY - globalOffsetY));
            }
        }

        // --- PREVIEW DA LINHA ELÁSTICA ---
        if ((functions.getFunctionSelected() == HandleFunctions.FunctionType.LINE
                || functions.getFunctionSelected() == HandleFunctions.FunctionType.POLYLINE)
                && functions.getTempStartPoint() != null
                && functions.getTempEndPoint() != null) {

            gc.setStroke(Color.WHITE);
            gc.setLineWidth(1.0 / scale);
            gc.setLineDashes(5.0 / scale);

            TopoPoint start = functions.getTempStartPoint();
            TopoPoint end = functions.getTempEndPoint();

            gc.strokeLine(
                    start.getX() - globalOffsetX, -(start.getY() - globalOffsetY),
                    end.getX() - globalOffsetX, -(end.getY() - globalOffsetY)
            );
            gc.setLineDashes(null);
        }

        // =================================================================
        // LOOP 2: PONTOS E TEXTOS (RENDERIZADOS POR CIMA DAS LINHAS)
        // =================================================================
        gc.setFont(javafx.scene.text.Font.font("Arial", fontSize));

        for (TopoObject obj : objects) {
            if (!isLayerVisible(obj.getLayerName())) continue;

            // Tabelas já foram desenhadas no Loop 1
            if (obj instanceof com.brasens.model.objects.TopoTableObject) continue;

            boolean isTextLayer = "TEXT".equals(obj.getLayerName());

            TopoLineType style = obj.getType();
            boolean isContour = (style == TopoLineType.CURVA_MESTRA || style == TopoLineType.CURVA_INTERMEDIARIA);

            for (TopoPoint p : obj.getPoints()) {
                double drawX = p.getX() - globalOffsetX;
                double drawY = -(p.getY() - globalOffsetY);

                if (isTextLayer) {
                    // Texto UI
                    if (p.isSelected()) gc.setFill(Color.ORANGERED);
                    else gc.setFill(Color.WHITE);

                    if (p.getName() != null) gc.fillText(p.getName(), drawX, drawY);

                    if (p.isSelected()) {
                        double s = 3 / scale;
                        gc.setStroke(Color.ORANGERED);
                        gc.setLineWidth(1 / scale);
                        gc.strokeLine(drawX - s, drawY - s, drawX + s, drawY + s);
                        gc.strokeLine(drawX - s, drawY + s, drawX + s, drawY - s);
                    }
                } else {
                    // Pontos de Geometria
                    if (isContour) {
                        if (p.isSelected()) {
                            gc.setFill(Color.ORANGERED);
                            double selSize = pointSize * 1.5;
                            gc.fillOval(drawX - selSize / 2, drawY - selSize / 2, selSize, selSize);
                        }
                        continue;
                    }

                    if (p.isSelected()) {
                        gc.setFill(Color.ORANGERED);
                        double selSize = pointSize * 1.5;
                        gc.fillOval(drawX - selSize / 2, drawY - selSize / 2, selSize, selSize);
                    } else {
                        gc.setFill(Color.YELLOW);
                        gc.fillOval(drawX - pointSize / 2, drawY - pointSize / 2, pointSize, pointSize);
                    }

                    // Nome do Ponto
                    gc.setFill(Color.WHITE);
                    if (p.getName() != null && !p.getName().isEmpty()) {
                        if (!p.getName().startsWith("INT")) {
                            gc.fillText(p.getName(), drawX + textOffset, drawY - textOffset);
                        }
                    }
                }
            }
        }

        // =================================================================
        // DESENHO DA CAIXA DE SELEÇÃO (WINDOW / CROSSING)
        // =================================================================
        if (functions.isSelectingBox() && functions.getSelectionStartScreenPoint() != null && functions.getSelectionCurrentScreenPoint() != null) {
            gc.setTransform(new Affine()); // Reseta para coordenadas de tela

            Vector2D start = functions.getSelectionStartScreenPoint();
            Vector2D end = functions.getSelectionCurrentScreenPoint();

            double x = Math.min(start.x(), end.x());
            double y = Math.min(start.y(), end.y());
            double w = Math.abs(end.x() - start.x());
            double h = Math.abs(end.y() - start.y());

            boolean isCrossing = end.x() < start.x(); // Dir -> Esq

            if (isCrossing) {
                // CROSSING (Verde, Tracejado)
                gc.setFill(crossingSelectionFillColor);
                gc.setStroke(crossingSelectionColor);
                gc.setLineDashes(5, 5);
            } else {
                // WINDOW (Azul, Sólido)
                gc.setFill(selectionFillColor);
                gc.setStroke(selectionColor);
                gc.setLineDashes(null);
            }

            gc.setLineWidth(1);
            gc.fillRect(x, y, w, h);
            gc.strokeRect(x, y, w, h);
        }

        // =================================================================
        // HUD (ROSA DOS VENTOS)
        // =================================================================
        if (isLayerVisible("ROSA_VENTOS") && compassImage != null) {
            gc.setTransform(new Affine()); // Reseta para tela

            double xPos = getWidth() - COMPASS_SIZE - COMPASS_MARGIN + 5;
            double yPos = getHeight() - COMPASS_SIZE - COMPASS_MARGIN - 5;

            double angleRad = Math.atan2(trans.getMyx(), trans.getMxx());
            double angleDeg = Math.toDegrees(angleRad);

            gc.save();
            gc.translate(xPos + COMPASS_SIZE / 2, yPos + COMPASS_SIZE / 2);
            gc.rotate(angleDeg);
            gc.translate(-COMPASS_SIZE / 2, -COMPASS_SIZE / 2);
            gc.drawImage(compassImage, 0, 0, COMPASS_SIZE, COMPASS_SIZE);
            gc.restore();
        }
    }

    private void drawResizeHandles(GraphicsContext gc, com.brasens.model.objects.TopoTableObject table, double scale) {
        TopoPoint origin = table.getPoints().get(0);

        // Coordenadas de desenho (Tela)
        double startX = origin.getX() - globalOffsetX;
        double startY = -(origin.getY() - globalOffsetY);

        double totalW = table.getTotalWidth();
        double totalH = table.getTotalHeight();

        // Tamanho fixo na tela (10px), independente do zoom
        double gripSize = 10.0 / scale;

        gc.setFill(Color.CYAN); // Cor interna bem visível
        gc.setStroke(Color.BLUE); // Borda
        gc.setLineWidth(1.5 / scale); // Borda um pouco mais grossa

        // 1. Grip DIREITA
        double rx = startX + totalW;
        double ry = startY + (totalH / 2);
        drawGripCircle(gc, rx, ry, gripSize);

        // 2. Grip BAIXO
        double bx = startX + (totalW / 2);
        double by = startY + totalH;
        drawGripCircle(gc, bx, by, gripSize);

        // 3. Grip CANTO
        double cx = startX + totalW;
        double cy = startY + totalH;
        drawGripCircle(gc, cx, cy, gripSize);
    }

    private void drawGripCircle(GraphicsContext gc, double centerX, double centerY, double size) {
        gc.fillOval(centerX - size/2, centerY - size/2, size, size);
        gc.strokeOval(centerX - size/2, centerY - size/2, size, size);
    }

    private com.brasens.model.objects.TopoTableObject findTableAtScreenPos(double screenX, double screenY) {
        for (TopoObject obj : objects) {
            if (obj instanceof com.brasens.model.objects.TopoTableObject) {
                com.brasens.model.objects.TopoTableObject tbl = (com.brasens.model.objects.TopoTableObject) obj;
                if (!isLayerVisible(tbl.getLayerName())) continue;

                TopoPoint origin = tbl.getPoints().get(0);

                // Converte a origem da tabela para Tela
                Vector2D screenOrigin = worldToScreen(origin.getX(), origin.getY());

                // Calcula largura e altura em pixels (Tela)
                double scale = trans.getMxx();
                double w = tbl.getTotalWidth() * scale;
                double h = tbl.getTotalHeight() * scale;

                // Verifica se o mouse está dentro do retângulo da tabela
                if (screenX >= screenOrigin.x() && screenX <= screenOrigin.x() + w &&
                        screenY >= screenOrigin.y() && screenY <= screenOrigin.y() + h) {
                    return tbl;
                }
            }
        }
        return null;
    }

    private void performWindowSelection() {
        Vector2D startScreen = functions.getSelectionStartScreenPoint();
        Vector2D endScreen = functions.getSelectionCurrentScreenPoint();

        if (startScreen == null || endScreen == null) return;

        // 1. Determina o retângulo de seleção na tela
        double screenMinX = Math.min(startScreen.x(), endScreen.x());
        double screenMaxX = Math.max(startScreen.x(), endScreen.x());
        double screenMinY = Math.min(startScreen.y(), endScreen.y());
        double screenMaxY = Math.max(startScreen.y(), endScreen.y());

        // Evita seleção de clique simples (arraste muito pequeno)
        if (Math.abs(screenMaxX - screenMinX) < 2 && Math.abs(screenMaxY - screenMinY) < 2) return;

        // 2. Determina o MODO: Window (Esq->Dir) ou Crossing (Dir->Esq)
        boolean isCrossing = endScreen.x() < startScreen.x();

        // 3. Converte o retângulo da tela para coordenadas do MUNDO
        Vector2D worldP1 = screenToWorld(screenMinX, screenMaxY); // Canto inferior esquerdo tela -> mundo
        Vector2D worldP2 = screenToWorld(screenMaxX, screenMinY); // Canto superior direito tela -> mundo

        double worldMinX = Math.min(worldP1.x(), worldP2.x());
        double worldMaxX = Math.max(worldP1.x(), worldP2.x());
        double worldMinY = Math.min(worldP1.y(), worldP2.y());
        double worldMaxY = Math.max(worldP1.y(), worldP2.y());

        // 4. Itera sobre os objetos e verifica
        for (TopoObject obj : objects) {
            if (!isLayerVisible(obj.getLayerName())) continue;
            if (obj.getPoints().isEmpty()) continue;

            boolean shouldSelect = false;

            if (isCrossing) {
                // MODO CROSSING (Verde): Basta tocar
                // Verifica se a Bounding Box do objeto intercepta a da seleção
                if (objectIntersectsRect(obj, worldMinX, worldMinY, worldMaxX, worldMaxY)) {
                    shouldSelect = true;
                }
            } else {
                // MODO WINDOW (Azul): Tudo deve estar dentro
                if (objectIsFullyInsideRect(obj, worldMinX, worldMinY, worldMaxX, worldMaxY)) {
                    shouldSelect = true;
                }
            }

            // Aplica a seleção aos pontos do objeto
            if (shouldSelect) {
                for (TopoPoint p : obj.getPoints()) {
                    p.setSelected(true);
                }
            }
        }
    }

    // Verifica se TODOS os pontos estão dentro
    private boolean objectIsFullyInsideRect(TopoObject obj, double minX, double minY, double maxX, double maxY) {
        for (TopoPoint p : obj.getPoints()) {
            if (p.getX() < minX || p.getX() > maxX || p.getY() < minY || p.getY() > maxY) {
                return false; // Um ponto fora já invalida
            }
        }
        return true;
    }

    // Verifica intersecção de Bounding Boxes (Simplificado para Crossing)
    private boolean objectIntersectsRect(TopoObject obj, double rMinX, double rMinY, double rMaxX, double rMaxY) {
        // 1. Calcula Bounding Box do objeto
        double objMinX = Double.MAX_VALUE, objMinY = Double.MAX_VALUE;
        double objMaxX = -Double.MAX_VALUE, objMaxY = -Double.MAX_VALUE;
        for (TopoPoint p : obj.getPoints()) {
            objMinX = Math.min(objMinX, p.getX());
            objMinY = Math.min(objMinY, p.getY());
            objMaxX = Math.max(objMaxX, p.getX());
            objMaxY = Math.max(objMaxY, p.getY());
        }

        // 2. Verifica intersecção de retângulos (AABB intersection)
        return (objMinX <= rMaxX && objMaxX >= rMinX &&
                objMinY <= rMaxY && objMaxY >= rMinY);
    }

    private void drawTable(GraphicsContext gc, com.brasens.model.objects.TopoTableObject table, double scale) {
        TopoPoint origin = table.getPoints().get(0);

        double startX = origin.getX() - globalOffsetX;
        double startY = -(origin.getY() - globalOffsetY);

        // Dimensões
        double rowH = table.getRowHeight(); // Metros
        double colW = table.getColWidth();  // Metros
        double totalW = table.getTotalWidth();
        double totalH = table.getTotalHeight();

        gc.setLineWidth(1.0 / scale);
        gc.setStroke(Color.WHITE);
        gc.setFill(Color.WHITE);

        gc.setFont(javafx.scene.text.Font.font("Arial", FontWeight.NORMAL, table.getFontSize()));

        gc.strokeRect(startX, startY, totalW, totalH);

        for (int i = 0; i <= table.getDataPoints().size() + 1; i++) {
            double y = startY + (i * rowH);
            gc.strokeLine(startX, y, startX + totalW, y);
        }

        for (int i = 0; i <= table.getHeaders().length; i++) {
            double x = startX + (i * colW);
            gc.strokeLine(x, startY, x, startY + totalH);
        }

        double textOffX = colW * 0.1;
        double textOffY = rowH * 0.7;

        String[] headers = table.getHeaders();
        for (int i = 0; i < headers.length; i++) {
            gc.fillText(headers[i], startX + (i * colW) + textOffX, startY + textOffY);
        }

        double currentY = startY + rowH;
        for (TopoPoint p : table.getDataPoints()) {
            gc.fillText(p.getName(), startX + textOffX, currentY + textOffY);
            gc.fillText(String.format("%.2f", p.getY()), startX + colW + textOffX, currentY + textOffY);
            gc.fillText(String.format("%.2f", p.getX()), startX + (colW * 2) + textOffX, currentY + textOffY);
            gc.fillText(String.format("%.2f", p.getZ()), startX + (colW * 3) + textOffX, currentY + textOffY);

            currentY += rowH;
        }

        if (isObjectSelected(table)) {
            gc.setStroke(Color.ORANGERED);
            gc.setLineWidth(2.0 / scale);
            gc.strokeRect(startX - (0.5/scale), startY - (0.5/scale), totalW + (1/scale), totalH + (1/scale));
        }
    }

    private void showContextMenu(MouseEvent e, TopoObject obj) {
        contextMenu = new ContextMenu();
        Vector2D clickPos = screenToWorld(e.getX(), e.getY());

        MenuItem itemPaste = new MenuItem("Colar Aqui");
        itemPaste.setDisable(CLIPBOARD.isEmpty());
        itemPaste.setOnAction(ev -> pasteFromClipboard(clickPos));

        contextMenu.getItems().add(itemPaste);

        if (obj != null) {
            contextMenu.getItems().add(new javafx.scene.control.SeparatorMenuItem());

            MenuItem titleItem = new MenuItem("Objeto: " + (obj.getId() != null ? obj.getId() : "Sem ID"));
            titleItem.setDisable(true);
            titleItem.setStyle("-fx-font-weight: bold; -fx-opacity: 1.0;");
            contextMenu.getItems().add(titleItem);

            MenuItem itemCut = new MenuItem("Recortar");
            itemCut.setOnAction(ev -> {
                copyObjectToClipboard(obj); // Copia
                objects.remove(obj);        // Deleta
                if (onContentChange != null) onContentChange.run();
                redraw();
                System.out.println("Objeto recortado.");
            });

            MenuItem itemCopy = new MenuItem("Copiar");
            itemCopy.setOnAction(ev -> copyObjectToClipboard(obj));

            MenuItem itemMove = new MenuItem("Mover Objeto");
            itemMove.setOnAction(ev -> {
                // Ativa o modo de movimento
                functions.setFunction(HandleFunctions.FunctionType.MOVE_OBJECT);
                functions.setObjectToMove(obj);
                // Define o ponto inicial do movimento como a posição atual do mouse no mundo
                functions.setMoveReferencePoint(clickPos);
                System.out.println("Movendo objeto...");
            });

            MenuItem itemRotate = new MenuItem("Rotacionar...");
            itemRotate.setOnAction(ev -> handleRotateObject(obj));

            MenuItem itemDelete = new MenuItem("Excluir");
            itemDelete.setStyle("-fx-text-fill: red;");
            itemDelete.setOnAction(ev -> {
                objects.remove(obj);
                if (onContentChange != null) onContentChange.run();
                redraw();
            });

            contextMenu.getItems().addAll(itemCut, itemCopy, itemMove, itemRotate, new javafx.scene.control.SeparatorMenuItem(), itemDelete);

            javafx.scene.control.Menu menuStyles = new javafx.scene.control.Menu("Estilo de Linha");
            for (TopoLineType type : TopoLineType.values()) {
                MenuItem item = new MenuItem(type.getLabel());
                javafx.scene.shape.Rectangle icon = new javafx.scene.shape.Rectangle(12, 12, type.getColor());
                icon.setStroke(Color.BLACK);
                item.setGraphic(icon);
                item.setOnAction(event -> {
                    obj.setType(type);
                    redraw();
                });
                menuStyles.getItems().add(item);
            }
            contextMenu.getItems().add(new javafx.scene.control.SeparatorMenuItem());
            contextMenu.getItems().add(menuStyles);

            // PROPRIEDADES
            MenuItem itemProps = new MenuItem("Propriedades...");
            itemProps.setOnAction(ev -> handleProperties(obj));
            contextMenu.getItems().add(itemProps);
        }

        contextMenu.show(this, e.getScreenX(), e.getScreenY());
    }

    private void copyObjectToClipboard(TopoObject obj) {
        CLIPBOARD.clear();
        TopoObject clone = cloneObject(obj);
        CLIPBOARD.add(clone);
        System.out.println("Objeto copiado para o clipboard.");
    }

    private void pasteFromClipboard(Vector2D dropLocation) {
        if (CLIPBOARD.isEmpty()) return;

        TopoObject source = CLIPBOARD.get(0);
        TopoObject clone = cloneObject(source);

        double sumX = 0;
        double sumY = 0;
        int count = 0;

        for(TopoPoint p : source.getPoints()) {
            sumX += p.getX();
            sumY += p.getY();
            count++;
        }

        double centerX = (count > 0) ? sumX / count : 0;
        double centerY = (count > 0) ? sumY / count : 0;

        double dx = dropLocation.x() - centerX;
        double dy = dropLocation.y() - centerY;

        // 3. Aplica o movimento
        for(TopoPoint p : clone.getPoints()) {
            p.setX(p.getX() + dx);
            p.setY(p.getY() + dy);
        }

        clone.setId(clone.getId() + "-COPY");
        this.objects.add(clone);
        redraw();
    }

    private TopoObject cloneObject(TopoObject source) {
        TopoObject clone = new TopoObject();
        clone.setId(source.getId());
        clone.setLayerName(source.getLayerName());
        clone.setType(source.getType());
        clone.setClosed(source.isClosed());

        // Clona pontos (Deep Copy)
        for (TopoPoint p : source.getPoints()) {
            TopoPoint np = new TopoPoint(p.getName(), p.getX(), p.getY());
            np.setZ(p.getZ());
            clone.addPoint(np);
        }

        // Se for tabela, copia atributos específicos
        if (source instanceof com.brasens.model.objects.TopoTableObject) {
            com.brasens.model.objects.TopoTableObject srcTbl = (com.brasens.model.objects.TopoTableObject) source;
            com.brasens.model.objects.TopoTableObject newTbl = new com.brasens.model.objects.TopoTableObject(
                    srcTbl.getPoints().get(0).getX(),
                    srcTbl.getPoints().get(0).getY(),
                    new ArrayList<>(srcTbl.getDataPoints()) // Copia lista
            );
            return newTbl;
        }

        return clone;
    }

    // --- LÓGICA DE ROTAÇÃO ---

    private void handleRotateObject(TopoObject obj) {
        javafx.scene.control.TextInputDialog dialog = new javafx.scene.control.TextInputDialog("0");
        dialog.setTitle("Rotacionar Objeto");
        dialog.setHeaderText("Digite o ângulo de rotação (graus):");
        dialog.setContentText("Ângulo (positivo = anti-horário):");

        dialog.showAndWait().ifPresent(val -> {
            try {
                double angleDeg = Double.parseDouble(val.replace(",", "."));
                rotateObjectGeometry(obj, angleDeg);
                redraw();
            } catch (NumberFormatException e) {
                System.err.println("Ângulo inválido");
            }
        });
    }

    private void rotateObjectGeometry(TopoObject obj, double angleDeg) {
        if (obj.getPoints().isEmpty()) return;

        double sumX = 0, sumY = 0;
        for (TopoPoint p : obj.getPoints()) {
            sumX += p.getX();
            sumY += p.getY();
        }
        double cx = sumX / obj.getPoints().size();
        double cy = sumY / obj.getPoints().size();

        // 2. Aplicar rotação em cada ponto
        double rad = Math.toRadians(angleDeg);
        double cos = Math.cos(rad);
        double sin = Math.sin(rad);

        for (TopoPoint p : obj.getPoints()) {
            double dx = p.getX() - cx;
            double dy = p.getY() - cy;

            double xNew = cx + (dx * cos - dy * sin);
            double yNew = cy + (dx * sin + dy * cos);

            p.setX(xNew);
            p.setY(yNew);
        }
    }

    // --- LÓGICA DE PROPRIEDADES ---
    private void handleProperties(TopoObject obj) {
        javafx.scene.control.TextInputDialog dialog = new javafx.scene.control.TextInputDialog(obj.getId());
        dialog.setTitle("Propriedades");
        dialog.setHeaderText("Renomear Identificador do Objeto:");
        dialog.setContentText("Novo ID:");

        dialog.showAndWait().ifPresent(newId -> {
            obj.setId(newId);
            redraw();
        });
    }

    private void drawSelectionCross(GraphicsContext gc, double x, double y, double scale) {
        double s = 3 / scale;
        gc.setStroke(Color.ORANGERED);
        gc.setLineWidth(1/scale);
        gc.strokeLine(x-s, y-s, x+s, y+s);
        gc.strokeLine(x-s, y+s, x+s, y-s);
    }

    private boolean isObjectSelected(TopoObject obj) {
        for (TopoPoint p : obj.getPoints()) {
            if (p.isSelected()) return true;
        }
        return false;
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
            javafx.geometry.Point2D p1 = trans.inverseTransform(0, 0);
            javafx.geometry.Point2D p2 = trans.inverseTransform(width, height);

            double minX_rel = Math.min(p1.getX(), p2.getX());
            double maxX_rel = Math.max(p1.getX(), p2.getX());
            double minY_rel = Math.min(p1.getY(), p2.getY());
            double maxY_rel = Math.max(p1.getY(), p2.getY());

            double minX_abs = minX_rel + globalOffsetX;
            double maxX_abs = maxX_rel + globalOffsetX;

            double targetPixelSpacing = 100.0;
            double rawStep = targetPixelSpacing / scale;
            double step = calculateNiceStep(rawStep);

            double fontSize = 12 / scale;
            gc.setFont(javafx.scene.text.Font.font("Arial", fontSize));
            gc.setFill(Color.GRAY);
            gc.setStroke(Color.rgb(100, 100, 100, 0.2));
            gc.setLineWidth(1 / scale);

            double textOffset = 4 / scale;

            double startX_abs = Math.floor(minX_abs / step) * step;

            for (double x_abs = startX_abs; x_abs <= maxX_abs; x_abs += step) {
                double drawX = x_abs - globalOffsetX;

                gc.strokeLine(drawX, minY_rel, drawX, maxY_rel);

                String label = String.format("E %.0f", x_abs);
                gc.fillText(label, drawX + textOffset, maxY_rel - (textOffset * 2));
            }

            double y_abs_start = (-minY_rel) + globalOffsetY;
            double y_abs_end = (-maxY_rel) + globalOffsetY;

            double min_y_abs = Math.min(y_abs_start, y_abs_end);
            double max_y_abs = Math.max(y_abs_start, y_abs_end);

            double startY_abs = Math.floor(min_y_abs / step) * step;

            for (double y_abs = startY_abs; y_abs <= max_y_abs; y_abs += step) {
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

    public TopoPoint getSingleSelectedTextPoint() {
        TopoPoint selected = null;
        int count = 0;

        for (TopoObject obj : objects) {
            if ("TEXT".equals(obj.getLayerName())) {
                for (TopoPoint p : obj.getPoints()) {
                    if (p.isSelected()) {
                        selected = p;
                        count++;
                    }
                }
            }
        }

        if (count == 1) {
            return selected;
        }

        return null;
    }

    public void deleteSelected() {
        if (objects.isEmpty()) return;

        List<TopoObject> objectsToRemove = new ArrayList<>();
        boolean somethingChanged = false;

        for (TopoObject obj : objects) {
            // 1. Remove apenas os pontos selecionados (que estão laranjas)
            boolean removed = obj.getPoints().removeIf(TopoPoint::isSelected);

            if (removed) {
                somethingChanged = true;
            }

            if (obj.getPoints().isEmpty()) {
                objectsToRemove.add(obj);
            }

            else if (!"TEXT".equals(obj.getLayerName()) &&
                    !"TABELA".equals(obj.getLayerName()) &&
                    !"NUVEM_PONTOS".equals(obj.getLayerName()) &&
                    obj.getPoints().size() < 2) {

                objectsToRemove.add(obj);
            }
        }

        if (somethingChanged || !objectsToRemove.isEmpty()) {
            objects.removeAll(objectsToRemove);

            if (onContentChange != null) onContentChange.run();
            if (onSelectionChanged != null) onSelectionChanged.accept(null);
            redraw();
            System.out.println("Itens deletados e limpeza realizada.");
        }
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

        if (contextMenu != null) contextMenu.hide();

        // 1. Verifica se clicou numa bolinha de resize (Prioridade)
        ResizeHandle handle = checkResizeHandles(e.getX(), e.getY());
        if (handle != ResizeHandle.NONE) {
            this.activeResizeHandle = handle;
            this.lastResizeMousePos = screenToWorld(e.getX(), e.getY());
            return;
        }

        // 2. Menu de Contexto
        if (e.getButton() == MouseButton.SECONDARY) {
            TopoPoint hitPoint = findPointNear(e.getX(), e.getY(), 10.0);
            TopoObject hitObj = null;
            if (hitPoint != null) {
                hitObj = findParentElement(hitPoint);
            } else {
                // Se não achou ponto, vê se clicou numa tabela para mostrar menu dela
                hitObj = findTableAtScreenPos(e.getX(), e.getY());
            }
            showContextMenu(e, hitObj);
            return;
        }

        functions.updateMousePosition(e.getX(), e.getY());
        functions.startDrag();
        startDragX = e.getX();
        startDragY = e.getY();
        isDraggingConfirmed = false;

        TopoPoint hitPoint = findPointNear(e.getX(), e.getY(), 10.0);

        if (functions.getFunctionSelected() == HandleFunctions.FunctionType.NONE) {
            if (hitPoint != null) {
                // CLICOU EM PONTO
                functions.setPointBeingDragged(hitPoint);
                if (!hitPoint.isSelected()) {
                    if (!e.isControlDown() && !e.isShiftDown()) clearSelection();
                    hitPoint.setSelected(true);
                }
                if (onSelectionChanged != null) onSelectionChanged.accept(hitPoint);
                redraw();
                return;
            } else {
                // NÃO CLICOU EM PONTO -> Tenta Tabela ou Seleção por Caixa

                // --- NOVO: Verifica se clicou no corpo de uma Tabela ---
                com.brasens.model.objects.TopoTableObject hitTable = findTableAtScreenPos(e.getX(), e.getY());

                if (hitTable != null) {
                    // Seleciona a tabela inteira
                    if (!e.isControlDown() && !e.isShiftDown()) clearSelection();

                    // Marca todos os pontos da tabela como selecionados (para ativar o isObjectSelected)
                    for(TopoPoint p : hitTable.getPoints()) p.setSelected(true);

                    redraw(); // Isso vai disparar o drawResizeHandles
                    return;
                }

                // --- Se não clicou em nada, inicia Seleção por Caixa ---
                if (!e.isControlDown() && !e.isShiftDown()) clearSelection();
                functions.setSelectingBox(true);
                functions.setSelectionStartScreenPoint(new Vector2D(e.getX(), e.getY()));
                functions.setSelectionCurrentScreenPoint(new Vector2D(e.getX(), e.getY()));
                if (onSelectionChanged != null) onSelectionChanged.accept(null);
                redraw();
                return;
            }
        }

        // ... Logica de double click mantida ...
        double dTime = (System.nanoTime() - clickTime) / 1e6;
        clickTime = System.nanoTime();
        if (dTime < 250) functions.handleDoubleClick(new Vector2D(lastMouseX, lastMouseY), this);
        else functions.handleClick(new Vector2D(lastMouseX, lastMouseY), this);
    }

    private TopoObject findParentElement(TopoPoint p) {
        for (TopoObject obj : objects) {
            if (obj.getPoints().contains(p)) {
                return obj;
            }
        }
        return null;
    }

    private ResizeHandle checkResizeHandles(double mouseX, double mouseY) {
        // Tolerância em PIXELS (área de clique confortável)
        double tolerance = 10.0;

        for (TopoObject obj : objects) {
            // Só verifica objetos que são Tabela E estão selecionados
            if (obj instanceof com.brasens.model.objects.TopoTableObject && isObjectSelected(obj)) {
                com.brasens.model.objects.TopoTableObject table = (com.brasens.model.objects.TopoTableObject) obj;

                TopoPoint origin = table.getPoints().get(0);

                // Converte origem da tabela (Mundo) para Tela
                Vector2D screenOrigin = worldToScreen(origin.getX(), origin.getY());

                // Calcula dimensões na tela baseadas no zoom atual
                double scale = trans.getMxx();
                double screenW = table.getTotalWidth() * scale;
                double screenH = table.getTotalHeight() * scale;

                double xBase = screenOrigin.x();
                double yBase = screenOrigin.y();

                // --- Coordenadas dos Grips na Tela ---

                // 1. Direita (Middle Right)
                double rx = xBase + screenW;
                double ry = yBase + (screenH / 2);
                if (getDistance(mouseX, mouseY, rx, ry) <= tolerance) {
                    this.resizingTable = table;
                    return ResizeHandle.RIGHT;
                }

                // 2. Baixo (Bottom Center)
                double bx = xBase + (screenW / 2);
                double by = yBase + screenH;
                if (getDistance(mouseX, mouseY, bx, by) <= tolerance) {
                    this.resizingTable = table;
                    return ResizeHandle.BOTTOM;
                }

                // 3. Canto (Bottom Right)
                double cx = xBase + screenW;
                double cy = yBase + screenH;
                if (getDistance(mouseX, mouseY, cx, cy) <= tolerance) {
                    this.resizingTable = table;
                    return ResizeHandle.CORNER;
                }
            }
        }
        return ResizeHandle.NONE;
    }

    private double getDistance(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }

    private void handleMouseReleased(MouseEvent e) {
        if (activeResizeHandle != ResizeHandle.NONE) {
            activeResizeHandle = ResizeHandle.NONE;
            resizingTable = null;
            lastResizeMousePos = null;
            return;
        }

        if (functions.getPointBeingDragged() != null) {
            System.out.println("Moveu ponto para: " + functions.getPointBeingDragged().getX() + ", " + functions.getPointBeingDragged().getY());

            if (onContentChange != null) onContentChange.run();

            functions.setPointBeingDragged(null);
        }

        if (functions.isSelectingBox()) {
            performWindowSelection(); // <--- Método novo que faremos abaixo

            // Reseta estado
            functions.setSelectingBox(false);
            functions.setSelectionStartScreenPoint(null);
            functions.setSelectionCurrentScreenPoint(null);
            redraw();
        }

        functions.stopDrag();
    }

    private void handleMouseDragged(MouseEvent e) {
        functions.updateMousePosition(e.getX(), e.getY());

        // --- 0. NOVO: LÓGICA DE REDIMENSIONAMENTO DE TABELA ---
        // Se uma alça estiver ativa, calcula o novo tamanho e encerra
        if (activeResizeHandle != ResizeHandle.NONE && resizingTable != null) {
            Vector2D currentWorldPos = screenToWorld(e.getX(), e.getY());

            TopoPoint origin = resizingTable.getPoints().get(0);

            // Calcula distâncias no Mundo
            double distX = currentWorldPos.x() - origin.getX();
            // Inverte Y pois o mouse desce na tela (Y aumenta), mas no mundo Topo Y diminui
            double distY = origin.getY() - currentWorldPos.y();

            // Travas de segurança (mínimo de 1 metro)
            if (distX < 1.0) distX = 1.0;
            if (distY < 1.0) distY = 1.0;

            switch (activeResizeHandle) {
                case RIGHT: // Altera Largura da Coluna
                    // Largura = Distância X / Número de Colunas
                    double newColW = distX / resizingTable.getHeaders().length;
                    resizingTable.setColWidth(newColW);
                    break;

                case BOTTOM: // Altera Altura da Linha
                    // Altura = Distância Y / (Dados + Cabeçalho)
                    double newRowH = distY / (resizingTable.getDataPoints().size() + 1);
                    resizingTable.setRowHeight(newRowH);
                    break;

                case CORNER: // Escala Geral (Proporcional)
                    // Usa a altura como base para tudo
                    double totalRows = resizingTable.getDataPoints().size() + 1;
                    double masterH = distY / totalRows;

                    resizingTable.setRowHeight(masterH);
                    // Mantém a proporção visual: Fonte = 50% da linha, Coluna = 400% da linha
                    resizingTable.setFontSize(masterH * 0.5);
                    resizingTable.setColWidth(masterH * 4.0);
                    break;
            }

            redraw();
            return; // Interrompe para não processar outros arrastes
        }

        // --- 1. SELEÇÃO POR CAIXA (WINDOW/CROSSING) ---
        if (functions.isSelectingBox()) {
            functions.setSelectionCurrentScreenPoint(new Vector2D(e.getX(), e.getY()));
            redraw(); // Redesenha a caixa azul/verde
            return; // Importante: não faz pan nem arrasta pontos
        }

        // --- 2. ARRASTAR PONTO EXISTENTE ---
        if (functions.getPointBeingDragged() != null && functions.getFunctionSelected() == HandleFunctions.FunctionType.NONE) {
            Vector2D worldPos = screenToWorld(e.getX(), e.getY());

            if (!isDraggingConfirmed) {
                double deltaX = e.getX() - startDragX;
                double deltaY = e.getY() - startDragY;
                double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);

                if (distance < DRAG_THRESHOLD) {
                    return;
                } else {
                    isDraggingConfirmed = true;
                }
            }

            functions.getPointBeingDragged().setX(worldPos.x());
            functions.getPointBeingDragged().setY(worldPos.y());

            if (onSelectionChanged != null) {
                onSelectionChanged.accept(functions.getPointBeingDragged());
            }

            redraw();
            return;
        }

        // --- 3. PAN (MOVER A TELA) ---
        if (e.getButton() == MouseButton.MIDDLE || e.getButton() == MouseButton.PRIMARY) {
            double dx = e.getX() - lastMouseX;
            double dy = e.getY() - lastMouseY;

            try {
                trans.prependTranslation(dx, dy);
                redraw();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        lastMouseX = e.getX();
        lastMouseY = e.getY();
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

    public List<TopoPoint> getAllPoints() {
        List<TopoPoint> all = new ArrayList<>();
        for(TopoObject obj : objects) {
            all.addAll(obj.getPoints());
        }
        return all;
    }
}