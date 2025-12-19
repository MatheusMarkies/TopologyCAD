package com.brasens.layout.components.charts;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CustomLineChart extends VBox {
    private  NumberAxis yAxis = new NumberAxis();
    private javafx.scene.chart.LineChart<String, Number> chart = new javafx.scene.chart.LineChart<>(new CategoryAxis(), yAxis);
    ObservableList<XYChart.Series<String, Number>> series = FXCollections.observableArrayList();
    public CustomLineChart(){
        AnchorPane.setBottomAnchor(this, 0.0);
        AnchorPane.setLeftAnchor(this, 0.0);
        AnchorPane.setRightAnchor(this, 0.0);
        AnchorPane.setTopAnchor(this, 0.0);

        chart.setHorizontalGridLinesVisible(false);
        chart.setVerticalGridLinesVisible(false);

        getChildren().add(chart);
    }
    private void createGuideLine(XYChart.Data<String, Number> dataPoint) {
        Line guideLine = new Line();

        guideLine.setStartX(Double.parseDouble(dataPoint.getXValue()));
        guideLine.setStartY(dataPoint.getYValue().doubleValue());
        guideLine.setEndX(Double.parseDouble(dataPoint.getXValue()));
        guideLine.setEndY(0);
        guideLine.setStroke(Color.RED);
    }
}
