package com.brasens.layout.components.charts;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CustomBarChart  extends VBox {
    private NumberAxis yAxis = new NumberAxis();
    private javafx.scene.chart.BarChart<String, Number> chart = new javafx.scene.chart.BarChart<>(new CategoryAxis(), yAxis);
    ObservableList<XYChart.Series<String, Number>> series = FXCollections.observableArrayList();
    public CustomBarChart(){
        AnchorPane.setBottomAnchor(this, 0.0);
        AnchorPane.setLeftAnchor(this, 0.0);
        AnchorPane.setRightAnchor(this, 0.0);
        AnchorPane.setTopAnchor(this, 0.0);

        chart.setHorizontalGridLinesVisible(false);
        chart.setVerticalGridLinesVisible(false);

        getChildren().add(chart);
    }
}
