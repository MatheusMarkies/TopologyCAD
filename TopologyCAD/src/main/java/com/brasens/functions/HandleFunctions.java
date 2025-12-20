package com.brasens.functions;

import com.brasens.layout.components.CAD.Canvas.CadCanvas;
import com.brasens.model.objects.TopoPoint;
import com.brasens.utilities.math.Vector2D;
import javafx.animation.AnimationTimer;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.ScrollEvent;
import lombok.Getter;
import lombok.Setter;

import java.util.Optional;

@Getter @Setter
public class HandleFunctions {
    private double zoomFactor = 1.1;
    private double pivotX, pivotY = 0;

    private boolean wasEdgePanEnabled = false;

    private double edgeThreshold = 50.0;
    private double panSpeed = 10.0;

    private double currentMouseX = 0;
    private double currentMouseY = 0;

    private double edgeDx = 0;
    private double edgeDy = 0;

    private AnimationTimer edgePanTimer;

    private boolean edgePanEnabled = false;

    public enum FunctionType {
        NONE, EDGEPAN, LINE, POLYLINE, TEXT
    }

    private FunctionType functionSelected = FunctionType.NONE;

    private TopoPoint tempStartPoint = null;
    private TopoPoint tempEndPoint = null;

    private TopoPoint pointBeingDragged = null;

    public void setFunction(FunctionType type) {
        this.functionSelected = type;
        this.tempStartPoint = null;
        this.tempEndPoint = null;
        System.out.println("Ferramenta selecionada: " + type);
    }

    public void handleScroll(ScrollEvent e, CadCanvas canvasReference){
        if (e.getDeltaY() < 0)
            this.zoomFactor = 1 / 1.1;
        else
            this.zoomFactor = 1.1;

        this.pivotX = e.getX();
        this.pivotY = e.getY();
    }

    public void startDrag() {
        this.wasEdgePanEnabled = this.edgePanEnabled;
        this.edgePanEnabled = false;
    }

    public void stopDrag() {
        this.edgePanEnabled = this.wasEdgePanEnabled;
        this.edgeDx = 0;
        this.edgeDy = 0;
    }

    public void handleMouseMove(Vector2D screenPos, CadCanvas canvasReference) {
        // Agora aceita LINE ou POLYLINE
        boolean isDrawingTool = functionSelected == FunctionType.LINE || functionSelected == FunctionType.POLYLINE;

        if (isDrawingTool && tempStartPoint != null) {
            Vector2D worldPos = canvasReference.screenToWorld(screenPos.x(), screenPos.y());

            // Cria o ponto fantasma onde o mouse está
            this.tempEndPoint = new TopoPoint("GHOST", worldPos.x(), worldPos.y());
            canvasReference.redraw();
        }
    }

    public void updateEdgePan(double width, double height) {
        this.edgeDx = 0;
        this.edgeDy = 0;

        if (currentMouseX < 0 || currentMouseX > width || currentMouseY < 0 || currentMouseY > height) {
            return;
        }

        if (currentMouseX < edgeThreshold) {
            this.edgeDx = panSpeed;
        }
        else if (currentMouseX > width - edgeThreshold) {
            this.edgeDx = -panSpeed;
        }

        if (currentMouseY < edgeThreshold) {
            this.edgeDy = panSpeed;
        }
        else if (currentMouseY > height - edgeThreshold) {
            this.edgeDy = -panSpeed;
        }
    }

    public void updateMousePosition(double x, double y) {
        this.currentMouseX = x;
        this.currentMouseY = y;
    }

    public void cancelOperation(CadCanvas canvasReference) {
        this.functionSelected = FunctionType.NONE;
        this.tempStartPoint = null;
        this.tempEndPoint = null;

        // NOVO: Solta o ponto se estiver arrastando
        this.pointBeingDragged = null;

        stopDrag();
        canvasReference.finishPolyLine();
        canvasReference.clearSelection();
        canvasReference.redraw();

        System.out.println("Operação cancelada (ESC).");
    }

    public void handleClick(Vector2D pointer, CadCanvas canvasReference){
        Vector2D worldPos = canvasReference.screenToWorld(pointer.x(), pointer.y());

        switch (functionSelected){
            case NONE -> {
                TopoPoint hitPoint = canvasReference.findPointNear(pointer.x(), pointer.y(), 10.0);

                if (hitPoint != null) {
                    System.out.println("Ponto Selecionado: " + hitPoint.getName());

                    boolean wasSelected = hitPoint.isSelected();

                    canvasReference.clearSelection();

                    if (!wasSelected) {
                        hitPoint.setSelected(true);
                    }
                } else {
                    canvasReference.clearSelection();
                }

                canvasReference.redraw();
            }
            case EDGEPAN -> {

            }
            case LINE -> {
                if (tempStartPoint == null) {
                    String name = canvasReference.getNextPointName();

                    tempStartPoint = new TopoPoint(name, worldPos.x(), worldPos.y());
                    edgePanEnabled = true;
                } else {
                    String name = canvasReference.getNextPointName(tempStartPoint.getName());

                    TopoPoint finalEnd = new TopoPoint(name, worldPos.x(), worldPos.y());
                    canvasReference.addLineObject(tempStartPoint, finalEnd);

                    tempStartPoint = null;
                    tempEndPoint = null;
                    edgePanEnabled = false;
                    canvasReference.redraw();
                }
            }
            case POLYLINE -> {
                String ptName = "PT-" + (canvasReference.getObjects().size() + 100);

                if (tempStartPoint == null) {
                    edgePanEnabled = true;

                    String name = canvasReference.getNextPointName();

                    tempStartPoint = new TopoPoint(name, worldPos.x(), worldPos.y());
                    canvasReference.createPolyLine(tempStartPoint);

                } else {
                    String name = canvasReference.getNextPointName();

                    TopoPoint newPoint = new TopoPoint(name, worldPos.x(), worldPos.y());
                    canvasReference.addPointToPolyLine(newPoint);

                    tempStartPoint = newPoint;
                }
            }
            case TEXT -> {
                TextInputDialog dialog = new TextInputDialog();
                dialog.setTitle("Inserir Texto");
                dialog.setHeaderText("Digite o conteúdo do texto:");
                dialog.setContentText("Texto:");

                Optional<String> result = dialog.showAndWait();

                result.ifPresent(text -> {
                    if (!text.isEmpty()) {
                        canvasReference.addTextObject(worldPos.x(), worldPos.y(), text);
                    }
                });

            }
        }
    }
    public void handleDoubleClick(Vector2D pointer, CadCanvas canvasReference){
        switch (functionSelected){
            case EDGEPAN -> {

            }
            case LINE -> {

            }
            case POLYLINE -> {
                canvasReference.finishPolyLine();

                tempStartPoint = null;
                tempEndPoint = null;
                edgePanEnabled = false;
                canvasReference.redraw();
            }
            case TEXT -> {

            }
        }
    }
}