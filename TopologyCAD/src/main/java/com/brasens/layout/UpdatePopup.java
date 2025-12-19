package com.brasens.layout;

import com.brasens.CAD;
import com.brasens.model.Update;
import com.brasens.layout.components.CustomButton;
import com.brasens.utilities.common.FilesManager;
import com.brasens.utilities.common.ImageUtils;
import com.brasens.utilities.math.Interpolation;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

import java.awt.*;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;

import static com.brasens.layout.ApplicationWindow.BODY_ID;

public class UpdatePopup extends AnchorPane {

    public final Update update;
    String borderRadius =  "-fx-background-radius: 12px;";
    public static int width = 100;
    public static int height = 200;
    VBox mainVbox = new VBox();
    Separator separator = new Separator();
    CustomButton updateButton;
    ProgressBar downloadProgress = new ProgressBar();
    private javafx.scene.paint.Color backgroundButtonColor = Color.web("#0b1727"); //#3c5999

    public UpdatePopup(Update update){
        this.update = update;

        render();
    }

    public void render(){
        //setPrefSize(LayoutSizeManager.getResizedWidth(width), LayoutSizeManager.getResizedHeight(height));
        //setMaxSize(getPrefWidth(), getPrefHeight());
        getStyleClass().add("body");
        setId(BODY_ID);

        javafx.scene.text.Font versionNumberFont = new javafx.scene.text.Font(
                "Arial", (int)(22 * LayoutSizeManager.getInverseScreenAreaRatio()));
        javafx.scene.text.Font dearUserLabelFont = new javafx.scene.text.Font(
                "Arial", (int)(30 * LayoutSizeManager.getInverseScreenAreaRatio()));
        javafx.scene.text.Font textLabelFont = new javafx.scene.text.Font(
                "Arial", (int)(26 * LayoutSizeManager.getInverseScreenAreaRatio()));

        Label versionNumberLabel = new Label(update.getVersion());
        versionNumberLabel.setFont(versionNumberFont);

        HBox versionNumberHBox = new HBox();
        //versionNumberHBox.setMaxHeight(LayoutSizeManager.getResizedHeight(10));
        versionNumberHBox.setAlignment(Pos.CENTER);

        VBox.setVgrow(versionNumberHBox, Priority.NEVER);

        versionNumberHBox.getChildren().addAll(versionNumberLabel);

        mainVbox = new VBox();
        //vbox.setPrefHeight(LayoutSizeManager.getResizedHeight(height));
        //vbox.setPrefWidth(LayoutSizeManager.getResizedWidth(width));
        AnchorPane.setBottomAnchor(mainVbox, 10.0);
        AnchorPane.setLeftAnchor(mainVbox, 10.0);
        AnchorPane.setRightAnchor(mainVbox, 10.0);
        AnchorPane.setTopAnchor(mainVbox, 0.0);

        HBox imageHBox = new HBox();
        imageHBox.setAlignment(Pos.CENTER);

        VBox.setVgrow(imageHBox, Priority.ALWAYS);

        ImageView updateImage = new ImageView(
                new Image(CAD.class.getResource("/mspm/icons/default/rocket-lunch.png").toString())
        );

        updateImage.setFitHeight(124);
        updateImage.setFitWidth(124);

        imageHBox.getChildren().addAll(updateImage);
        VBox.setMargin(imageHBox, LayoutSizeManager.getResizedInsert(5.0,0.0, 0.0,0.0));

        Label dearUserLabel = new Label("Querido Usuário");
        dearUserLabel.setFont(dearUserLabelFont);

        HBox dearUserHBox = new HBox();
        dearUserHBox.setMaxHeight(LayoutSizeManager.getResizedHeight(60));
        dearUserHBox.setAlignment(Pos.BOTTOM_CENTER);

        VBox.setVgrow(dearUserHBox, Priority.NEVER);

        dearUserHBox.getChildren().addAll(dearUserLabel);

        Label textLabel = new Label("Uma nova versão do software está disponível! \n" +
                "Você pode instalar clicando no botão abaixo");
        textLabel.setFont(textLabelFont);
        textLabel.setWrapText(true);
        textLabel.setTextAlignment(TextAlignment.CENTER);
        HBox textHBox = new HBox();
        textHBox.setAlignment(Pos.BOTTOM_CENTER);

        VBox.setVgrow(textHBox, Priority.NEVER);

        textHBox.getChildren().addAll(textLabel);

        updateButton = new CustomButton(
                "Update",
                new Image(CAD.class.getResource("/mspm/icons/download.png").toString()),
                "-fx-background-color: transparent; "+borderRadius+"-fx-border-radius: 9px;-fx-border-width: 1px;-fx-border-color: #0b1727;",
                14
        );

        updateButton.setOnMouseEntered(event -> {
            final Animation animation = new Transition() {
                {
                    setCycleDuration(Duration.millis(200));
                    setInterpolator(Interpolator.EASE_OUT);
                }

                @Override
                protected void interpolate(double frac) {
                    Color backgroundColor = Interpolation.lerpColorFX(Color.web("#EDF2F9"), backgroundButtonColor, (float) frac);
                    Color textColor = Interpolation.lerpColorFX(backgroundButtonColor, Color.WHITE, (float) frac);
                    Color imageColor = Interpolation.lerpColorFX(Color.BLACK, Color.WHITE, (float) frac);

                    updateButton.getButtonLabelImageView().setImage(
                            ImageUtils.colorizeImage(new Image(CAD.class.getResource("/mspm/icons/download.png").toString()),imageColor)
                    );

                    updateButton.setStyle("-fx-background-color: "+ "rgba("
                            + (int) (backgroundColor.getRed() * 255) + ","
                            + (int) (backgroundColor.getGreen() * 255) + ","
                            + (int) (backgroundColor.getBlue() * 255) + ","
                            + backgroundColor.getOpacity() + "); -fx-background-radius: 6px;-fx-border-radius: 6px;-fx-border-width: 1px;-fx-border-color: #0b1727;");
                    updateButton.getButtonLabel().setTextFill(textColor);
                }
            };
            animation.play();
        });

        updateButton.setOnMouseExited(event -> {
            final Animation animation = new Transition() {
                {
                    setCycleDuration(Duration.millis(200));
                    setInterpolator(Interpolator.EASE_OUT);
                }

                @Override
                protected void interpolate(double frac) {
                    Color backgroundColor = Interpolation.lerpColorFX(backgroundButtonColor, Color.web("#EDF2F9"), (float) frac);
                    Color textColor = Interpolation.lerpColorFX(Color.WHITE, backgroundButtonColor, (float) frac);
                    Color imageColor = Interpolation.lerpColorFX(Color.WHITE, Color.BLACK, (float) frac);
                    //EDF2F9
                    updateButton.getButtonLabelImageView().setImage(
                            ImageUtils.colorizeImage(new Image(CAD.class.getResource("/mspm/icons/download.png").toString()),imageColor)
                    );

                    updateButton.setStyle("-fx-background-color: "+ "rgba("
                            + (int) (backgroundColor.getRed() * 255) + ","
                            + (int) (backgroundColor.getGreen() * 255) + ","
                            + (int) (backgroundColor.getBlue() * 255) + ","
                            + backgroundColor.getOpacity() + "); -fx-background-radius: 6px;-fx-border-radius: 6px;-fx-border-width: 1px;-fx-border-color: #0b1727;");
                    updateButton.getButtonLabel().setTextFill(textColor);
                }
            };
            animation.play();
        });

        updateButton.setMinHeight(LayoutSizeManager.getResizedHeight(45));

        VBox.setMargin(dearUserHBox, LayoutSizeManager.getResizedInsert(10.0,10.0, 10.0,0.0));
        dearUserLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold;");
        VBox.setMargin(textHBox, LayoutSizeManager.getResizedInsert(0.0,10.0, 10.0,0.0));

        VBox.setMargin(versionNumberHBox, LayoutSizeManager.getResizedInsert(2.0,0.0,0.0,0.0));

        separator = new Separator();
        separator.setMaxWidth(Double.MAX_VALUE);
        separator.setMaxHeight(5);

        HBox separatorHBox = new HBox();
        separatorHBox.setAlignment(Pos.CENTER);
        separatorHBox.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(separatorHBox, Priority.NEVER);

        separatorHBox.getChildren().addAll(separator);

        VBox.setMargin(separator, LayoutSizeManager.getResizedInsert(0.0,2.0, 0.0,10.0));

        mainVbox.getChildren().addAll(imageHBox, dearUserHBox, textHBox, versionNumberHBox, separator, updateButton);
        getChildren().addAll(mainVbox);

        updateButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                setProgressDownloadBar();

                Task<Void> task = new Task<Void>() {
                    @Override protected Void call() throws Exception {
                        downloadUpdate();
                        return null;
                    }

                    @Override protected void succeeded() {
                        super.succeeded();
                        updateMessage("Done!");
                    }

                    @Override protected void cancelled() {
                        super.cancelled();
                        updateMessage("Cancelled!");
                    }

                    @Override protected void failed() {
                        super.failed();
                        updateMessage("Failed!");
                    }
                };

                Thread thread = new Thread(task);
                thread.start();
            }
        });
    }

    public void setProgressDownloadBar() {
        HBox downloadHBox = new HBox();

        downloadHBox.setMaxHeight(LayoutSizeManager.getResizedHeight(60));
        downloadHBox.setAlignment(Pos.CENTER);

        VBox.setVgrow(downloadHBox, Priority.NEVER);

        downloadProgress = new ProgressBar();
        downloadProgress.setMaxWidth(Double.MAX_VALUE);
        downloadProgress.setStyle("-fx-accent: green");

        HBox.setHgrow(downloadProgress, Priority.ALWAYS);
        downloadHBox.getChildren().addAll(downloadProgress);

        VBox.setMargin(downloadHBox, LayoutSizeManager.getResizedInsert(0.0, 2.0, 2.0, 0.0));

        mainVbox.setCursor(Cursor.WAIT);

        mainVbox.getChildren().remove(updateButton);
        mainVbox.getChildren().addAll(downloadHBox);
    }

    public void downloadUpdate(){
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            try {
                File downloadedUpdate = new File(FilesManager.ApplicationDataFolder + "\\" + "updates" + "\\" + "Brasens MSPM.exe");

                URL url = new URL(update.getURL());
                URLConnection connection = url.openConnection();
                connection.connect();

                int fileLength = connection.getContentLength();

                InputStream inputStream = url.openStream();

                try (BufferedInputStream in = new BufferedInputStream(inputStream);
                     FileOutputStream fileOutputStream = new FileOutputStream(downloadedUpdate)) {
                    byte dataBuffer[] = new byte[1024];
                    int bytesRead;
                    int bytesDownloaded = 0;

                    long percent = 0;

                    while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                        fileOutputStream.write(dataBuffer, 0, bytesRead);
                        bytesDownloaded += 1024;

                        percent = Math.round(((double) bytesDownloaded / (double) fileLength) * 100);
                        long finalPercent = percent;
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                downloadProgress.setProgress((double) finalPercent/100);
                            }
                        });
                        //System.out.println(percent);
                    }
                } catch (IOException e) {
                    CAD.printNicerStackTrace(e);
                }

                Thread.sleep(5);
                Process process = Runtime.getRuntime().exec(downloadedUpdate.getAbsolutePath());

                System.exit(0);
            } catch (Exception e) {
                CAD.printNicerStackTrace(e);
            }
        }
    }

}