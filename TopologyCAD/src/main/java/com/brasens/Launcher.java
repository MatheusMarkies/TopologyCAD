package com.brasens;

import atlantafx.base.theme.PrimerLight;
import com.brasens.layout.ApplicationWindow;
import com.brasens.utilities.common.FilesManager;
import javafx.application.Application;
import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.update4j.LaunchContext;
import org.update4j.inject.InjectTarget;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Path;

public class Launcher implements org.update4j.service.Launcher {

    @Override
    public long version() {
        return 0;
    }

    @InjectTarget
    private Stage primaryStage;

    private static Stage stage;

    @Override
    public void run(LaunchContext ctx) {
        System.setProperty("http.keepAlive", "true");
        System.setProperty("prism.forceGPU", "true");
        System.setProperty("file.encoding", "UTF-8");
        JOptionPane.showInputDialog(null, "This is the message", "This is the default text");
        Platform.runLater(() -> {
            Thread.currentThread().setContextClassLoader(ctx.getClassLoader());

            stage = primaryStage;
        });

        Platform.runLater(() -> {
            FilesManager.applicationDirCreator();

            var root = new ApplicationWindow();
            var antialiasing = Platform.isSupported(ConditionalFeature.EFFECT)
                    ? SceneAntialiasing.BALANCED
                    : SceneAntialiasing.DISABLED;

            Scene scene = new Scene(root, ApplicationWindow.MIN_WIDTH + 80, 768, false, antialiasing);

            Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());

            scene.getStylesheets().add(getClass().getResource("/mspm/pages/DashboardCSS.css").toString());

            stage.setScene(scene);

            stage.setTitle("Brasens");

            Image brasensIcon = new Image(getClass().getResource("/mspm/resources/Icone File.png").toString());

            stage.getIcons().add(brasensIcon);
            stage.setScene(scene);

            stage.setOnCloseRequest(event -> {
                Platform.exit();
                System.exit(0);
            });

            stage.setMaximized(true);
            stage.show();
        });

    }

    private static boolean isEmptyDirectory(Path path) throws IOException {

        return false;
    }
}