package com.brasens.layout.controller.fxml;

import com.brasens.CAD;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

//Controller do FXML ActionStatusPopUp.fxml
public class ActionStatusPopUp {

    @FXML // fx:id="actionStatusImage"
    private ImageView actionStatusImage; // Value injected by FXMLLoader

    @FXML // fx:id="actionText"
    private Label actionText; // Value injected by FXMLLoader

    @FXML
    private AnchorPane main_pane;

    public enum ActionStatus{
        OK, ERROR
    }

    private boolean parametersSetted = false;

    public void setParameters(String actionText, ActionStatus actionStatus, Stage stage){
        this.actionText.setText(actionText);

        URL okImageURL = CAD.class.getResource("/mspm/resources/verified.png");
        URL errorImageURL = CAD.class.getResource("/mspm/resources/hide.png");
        Image okImage = new Image(okImageURL.toString());
        Image errorImage = new Image(errorImageURL.toString());

        this.stage = stage;

        if(actionStatus == ActionStatus.ERROR){
            this.actionStatusImage.setImage(errorImage);
        }else
            this.actionStatusImage.setImage(okImage);

        parametersSetted = true;
    }

    private Stage stage;

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    void initialize() {
        ScheduledExecutorService scheduledExecutorService;
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

        scheduledExecutorService.scheduleAtFixedRate(() -> {
            Platform.runLater(() -> {

            });
        }, 0, 10, TimeUnit.MILLISECONDS);
    }
}