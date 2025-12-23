package com.brasens.functions;

import com.brasens.layout.components.CAD.Canvas.CadCanvas;
import com.brasens.model.objects.TopoObject;
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
        NONE, EDGEPAN, LINE, POLYLINE, TEXT, PLACE_TABLE, MOVE_OBJECT
    }

    private FunctionType functionSelected = FunctionType.NONE;

    private TopoPoint tempStartPoint = null;
    private TopoPoint tempEndPoint = null;

    private TopoPoint pointBeingDragged = null;

    private TopoObject objectToMove = null;
    private Vector2D moveReferencePoint = null;

    private boolean isSelectingBox = false;
    private Vector2D selectionStartScreenPoint = null;
    private Vector2D selectionCurrentScreenPoint = null;

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
        // Lógica para Ferramentas de Desenho (Linha/Polilinha)
        boolean isDrawingTool = functionSelected == FunctionType.LINE || functionSelected == FunctionType.POLYLINE;

        if (isDrawingTool && tempStartPoint != null) {
            TopoPoint snapPoint = canvasReference.findPointNear(screenPos.x(), screenPos.y(), 15.0);
            double targetX, targetY;

            if (snapPoint != null) {
                targetX = snapPoint.getX();
                targetY = snapPoint.getY();
            } else {
                Vector2D worldPos = canvasReference.screenToWorld(screenPos.x(), screenPos.y());
                targetX = worldPos.x();
                targetY = worldPos.y();
            }

            // Cria/Atualiza o ponto fantasma
            this.tempEndPoint = new TopoPoint("GHOST", targetX, targetY);
            canvasReference.redraw();
        }

        // --- CORREÇÃO: Lógica de Mover Objeto (AGORA FORA DO BLOCO DE DESENHO) ---
        if (functionSelected == FunctionType.MOVE_OBJECT && objectToMove != null && moveReferencePoint != null) {
            Vector2D worldPos = canvasReference.screenToWorld(screenPos.x(), screenPos.y());

            double dx = worldPos.x() - moveReferencePoint.x();
            double dy = worldPos.y() - moveReferencePoint.y();

            // Move todos os pontos do objeto pelo delta calculado
            for (TopoPoint p : objectToMove.getPoints()) {
                p.setX(p.getX() + dx);
                p.setY(p.getY() + dy);
            }

            // Atualiza o ponto de referência para o atual
            moveReferencePoint = new Vector2D(worldPos.x(), worldPos.y());

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
        this.pointBeingDragged = null;
        this.objectToMove = null; // Limpa mover

        this.isSelectingBox = false;
        this.selectionStartScreenPoint = null;
        this.selectionCurrentScreenPoint = null;

        stopDrag();

        canvasReference.finishPolyLine();
        canvasReference.clearSelection();
        canvasReference.redraw();

        System.out.println("Operação cancelada (ESC).");
    }

    private TopoPoint getPointOrNew(Vector2D screenPos, CadCanvas canvas) {
        TopoPoint hitPoint = canvas.findPointNear(screenPos.x(), screenPos.y(), 10.0);

        if (hitPoint != null) {
            System.out.println("Snap no ponto: " + hitPoint.getName());
            return hitPoint;
        }

        Vector2D worldPos = canvas.screenToWorld(screenPos.x(), screenPos.y());
        String name = canvas.getNextPointName();
        return new TopoPoint(name, worldPos.x(), worldPos.y());
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
                TopoPoint clickedPoint = getPointOrNew(pointer, canvasReference);

                if (tempStartPoint == null) {
                    tempStartPoint = clickedPoint;
                    edgePanEnabled = true;
                } else {
                    if (clickedPoint != tempStartPoint) {
                        canvasReference.addLineObject(tempStartPoint, clickedPoint);
                    }

                    tempStartPoint = null;
                    tempEndPoint = null;
                    edgePanEnabled = false;
                    canvasReference.redraw();
                }
            }
            case POLYLINE -> {
                TopoPoint clickedPoint = getPointOrNew(pointer, canvasReference);

                if (tempStartPoint == null) {
                    edgePanEnabled = true;
                    tempStartPoint = clickedPoint;

                    canvasReference.createPolyLine(tempStartPoint);

                } else {
                    canvasReference.addPointToPolyLine(clickedPoint);
                    tempStartPoint = clickedPoint;
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

            case PLACE_TABLE -> {
                canvasReference.createTableAt(worldPos.x(), worldPos.y());
                setFunction(FunctionType.NONE);
                canvasReference.getScene().setCursor(javafx.scene.Cursor.DEFAULT);
            }

            case MOVE_OBJECT -> {
                System.out.println("Objeto movido com sucesso.");
                this.objectToMove = null;
                this.moveReferencePoint = null;
                setFunction(FunctionType.NONE);
                canvasReference.redraw();
            }
        }
    }

    public void handleDoubleClick(Vector2D pointer, CadCanvas canvasReference){
        switch (functionSelected){
            case POLYLINE -> {
                canvasReference.finishPolyLine();
                tempStartPoint = null;
                tempEndPoint = null;
                edgePanEnabled = false;
                canvasReference.redraw();
            }
            default -> {}
        }
    }
}