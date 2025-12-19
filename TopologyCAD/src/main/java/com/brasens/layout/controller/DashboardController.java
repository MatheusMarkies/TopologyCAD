package com.brasens.layout.controller;

import com.brasens.layout.ApplicationWindow;
import com.brasens.layout.view.DashboardView;
import com.brasens.utils.Controller;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.PieChart;

public class DashboardController extends Controller {
    DashboardView dashboardView;
    ApplicationWindow applicationWindow;

    public DashboardController(ApplicationWindow applicationWindow) {
        this.applicationWindow = applicationWindow;
        this.delay = 500;
    }

    @Override
    public void init() {
        dashboardView = applicationWindow.getViewManager().getDashboardView();

        Platform.runLater(() -> {
            if (dashboardView.getCadCanvas() != null) {
                dashboardView.getCadCanvas().redraw();
            }
        });
    }

    @Override
    public void close() {

    }

    @Override
    public void update() {

    }
}
