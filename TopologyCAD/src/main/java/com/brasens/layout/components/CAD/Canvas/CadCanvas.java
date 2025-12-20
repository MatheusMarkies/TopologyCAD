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

    public void redraw() {
        GraphicsContext gc = getGraphicsContext2D();

        gc.setTransform(new Affine());
        gc.setFill(Color.rgb(30, 30, 30));
        gc.fillRect(0, 0, getWidth(), getHeight());

        gc.setTransform(trans);

        drawGrid(gc);

        double scale = trans.getMxx();
        double pointSize = 5 / scale;
        double fontSize = 12 / scale;
        double textOffset = 8 / scale;

        for (TopoObject obj : objects) {

            if (!isLayerVisible(obj.getLayerName())) continue;
            if ("TEXT".equals(obj.getLayerName())) continue;

            List<TopoPoint> pts = obj.getPoints();
            if (pts.size() < 2) continue;

            TopoLineType style = obj.getType();
            boolean isContour = (style == TopoLineType.CURVA_MESTRA || style == TopoLineType.CURVA_INTERMEDIARIA);
            boolean isSelected = isObjectSelected(obj);

            if (isSelected) {
                gc.setStroke(Color.ORANGERED);
                gc.setLineWidth((style.getWidth() + 1.5) / scale);
            } else {
                gc.setStroke(style.getColor());
                gc.setLineWidth(style.getWidth() / scale);
            }

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

            if (style == TopoLineType.CURVA_MESTRA && pts.size() >= 2) {
                TopoPoint p1 = pts.get(0);
                TopoPoint p2 = pts.get(1);

                double midX = (p1.getX() + p2.getX()) / 2.0;
                double midY = (p1.getY() + p2.getY()) / 2.0;

                gc.setFill(style.getColor());
                gc.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, fontSize));

                gc.fillText(String.format("%.0f", p1.getZ()), midX - globalOffsetX, -(midY - globalOffsetY));
            }
        }

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

        gc.setFont(javafx.scene.text.Font.font("Arial", fontSize));

        for (TopoObject obj : objects) {
            if (!isLayerVisible(obj.getLayerName())) continue;

            boolean isTextLayer = "TEXT".equals(obj.getLayerName());
            TopoLineType style = obj.getType();
            boolean isContour = (style == TopoLineType.CURVA_MESTRA || style == TopoLineType.CURVA_INTERMEDIARIA);

            for (TopoPoint p : obj.getPoints()) {
                double drawX = p.getX() - globalOffsetX;
                double drawY = -(p.getY() - globalOffsetY);

                if (isTextLayer) {
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

                    gc.setFill(Color.WHITE);
                    if (p.getName() != null && !p.getName().isEmpty()) {
                        if (!p.getName().startsWith("INT")) {
                            gc.fillText(p.getName(), drawX + textOffset, drawY - textOffset);
                        }
                    }
                }
            }
        }

        if (isLayerVisible("ROSA_VENTOS") && compassImage != null) {
            gc.setTransform(new Affine());

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
            boolean removed = obj.getPoints().removeIf(TopoPoint::isSelected);

            if (removed) {
                somethingChanged = true;
            }

            if (obj.getPoints().isEmpty()) {
                objectsToRemove.add(obj);
            } else if (!"TEXT".equals(obj.getLayerName()) && obj.getPoints().size() < 2) {
                objectsToRemove.add(obj);
            }
        }

        if (somethingChanged) {
            objects.removeAll(objectsToRemove);

            if (onContentChange != null) onContentChange.run();
            if (onSelectionChanged != null) onSelectionChanged.accept(null);
            redraw();
            System.out.println("Itens deletados.");
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

        if (contextMenu != null) {
            contextMenu.hide();
        }

        if (e.getButton() == MouseButton.SECONDARY) {
            TopoPoint hitPoint = findPointNear(e.getX(), e.getY(), 10.0);

            if (hitPoint != null) {
                TopoObject parentObj = findParentElement(hitPoint);

                if (parentObj != null && !"TEXT".equals(parentObj.getLayerName())) {
                    showContextMenu(e, parentObj);
                    return;
                }
            }
        }

        functions.updateMousePosition(e.getX(), e.getY());
        functions.startDrag();

        startDragX = e.getX();
        startDragY = e.getY();
        isDraggingConfirmed = false;

        TopoPoint hitPoint = findPointNear(e.getX(), e.getY(), 10.0);

        if (functions.getFunctionSelected() == HandleFunctions.FunctionType.NONE) {
            if (hitPoint != null) {
                functions.setPointBeingDragged(hitPoint);

                if (!hitPoint.isSelected()) {
                    clearSelection();
                    hitPoint.setSelected(true);
                }

                if (onSelectionChanged != null) {
                    onSelectionChanged.accept(hitPoint);
                }

                redraw();
                return;
            } else {
                clearSelection();
                redraw();

                if (onSelectionChanged != null) {
                    onSelectionChanged.accept(null);
                }
            }
        }

        double dTime = (System.nanoTime() - clickTime) / 1e6;
        clickTime = System.nanoTime();

        if (dTime < 250) {
            functions.handleDoubleClick(new Vector2D(lastMouseX, lastMouseY), this);
        } else {
            functions.handleClick(new Vector2D(lastMouseX, lastMouseY), this);
        }
    }

    private TopoObject findParentElement(TopoPoint p) {
        for (TopoObject obj : objects) {
            if (obj.getPoints().contains(p)) {
                return obj;
            }
        }
        return null;
    }

    /**
     * Constrói e exibe o menu de contexto com as opções de TopoLineType.
     */
    private void showContextMenu(MouseEvent e, TopoObject obj) {
        contextMenu = new ContextMenu();

        for (TopoLineType type : TopoLineType.values()) {
            MenuItem item = new MenuItem(type.getLabel());

            javafx.scene.shape.Rectangle icon = new javafx.scene.shape.Rectangle(12, 12, type.getColor());
            icon.setStroke(Color.BLACK);
            icon.setStrokeWidth(0.5);
            item.setGraphic(icon);

            item.setOnAction(event -> {
                obj.setType(type);
                redraw();
                System.out.println("Objeto alterado para: " + type.getLabel());
            });

            contextMenu.getItems().add(item);
        }

        contextMenu.show(this, e.getScreenX(), e.getScreenY());
    }

    private void handleMouseReleased(MouseEvent e) {
        if (functions.getPointBeingDragged() != null) {
            System.out.println("Moveu ponto para: " + functions.getPointBeingDragged().getX() + ", " + functions.getPointBeingDragged().getY());

            if (onContentChange != null) onContentChange.run();

            functions.setPointBeingDragged(null);
        }

        functions.stopDrag();
    }

    private void handleMouseDragged(MouseEvent e) {
        functions.updateMousePosition(e.getX(), e.getY());

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