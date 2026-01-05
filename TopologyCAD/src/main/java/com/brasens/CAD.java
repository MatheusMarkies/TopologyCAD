package com.brasens;

import atlantafx.base.theme.PrimerLight;
import com.brasens.layout.ApplicationWindow;
import com.brasens.layout.DownloadPopup;
import com.brasens.layout.LayoutSizeManager;
import com.brasens.layout.controller.fxml.ActionStatusPopUp;
import com.brasens.utilities.FileDownload;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;

import static com.brasens.Config.APP_VERSION;
import static com.brasens.utilities.common.FilesManager.applicationDirCreator;

@Getter
@Setter
@SpringBootApplication
public class CAD extends Application {
    private Scene scene;
    private Stage primaryStageInstance;

    private ConfigurableApplicationContext springContext;
    private ApplicationWindow rootLayout;

    @Override
    public void init() throws Exception {
        springContext = new SpringApplicationBuilder(CAD.class)
                .headless(false)
                .run(getParameters().getRaw().toArray(new String[0]));

        rootLayout = springContext.getBean(ApplicationWindow.class);
    }

    @Override
    public void start(Stage stage) {
        try {
            applicationDirCreator();
            System.out.println("APP_VERSION: " + APP_VERSION);

            this.primaryStageInstance = stage;

            StageHolder stageHolder = springContext.getBean(StageHolder.class);
            stageHolder.setPrimaryStage(stage);

            var antialiasing = Platform.isSupported(ConditionalFeature.EFFECT)
                    ? SceneAntialiasing.BALANCED
                    : SceneAntialiasing.DISABLED;

            if (rootLayout == null) {
                System.err.println("ERRO CRÍTICO: rootLayout é nulo após initializeUI()! Não deveria acontecer.");
                Platform.exit();
                return;
            }
            scene = new Scene(rootLayout, ApplicationWindow.MIN_WIDTH + 80, 768, false, antialiasing);

            Application.setUserAgentStylesheet(new atlantafx.base.theme.PrimerDark().getUserAgentStylesheet());

            scene.getStylesheets().add(getClass().getResource("/mspm/pages/DashboardCSS.css").toString());

            stage.setScene(scene);
            stage.setTitle("Workbench");

            stage.setMaximized(true);
            stage.show();

        } catch (Throwable t) {
            t.printStackTrace(System.err);
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        System.setProperty("http.keepAlive", "true");
        System.setProperty("prism.forceGPU", "true");
        System.setProperty("file.encoding", "UTF-8");

        launch(args);
    }

    public static void printNicerStackTrace(Exception e) {
        StackTraceElement[] stackTraceElements = e.getStackTrace();

        System.err.println("Exception: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        System.err.println("Stack trace:");

        for (StackTraceElement element : stackTraceElements) {
            System.err.printf("  at %s.%s (%s:%d)%n",
                    element.getClassName(),
                    element.getMethodName(),
                    element.getFileName(),
                    element.getLineNumber());
        }
    }

    public static void openActionPopUp(String title, String text, ActionStatusPopUp.ActionStatus actionStatus) {
        try {
            FXMLLoader fxmlMain = new FXMLLoader(CAD.class.getResource("/mspm/ActionStatusPopUp.fxml"));
            Parent root = fxmlMain.load();

            Stage stage = new Stage();

            ActionStatusPopUp actionStatusPopUp = (ActionStatusPopUp) fxmlMain.getController();
            actionStatusPopUp.setParameters(text, actionStatus, stage);

            stage.setTitle(title);
            stage.setScene(new Scene(root));

            stage.show();

            int delayValue =3;

            if(actionStatus == ActionStatusPopUp.ActionStatus.ERROR)
                delayValue *= 2;

            PauseTransition delay = new PauseTransition(Duration.seconds(delayValue));
            delay.setOnFinished( event -> stage.close() );
            delay.play();

        } catch (IOException e) {
            printNicerStackTrace(e);
        }

    }

    public static void openDownloadPopUp(FileDownload file) {
        try {
            Stage stage = new Stage();
            var root = new DownloadPopup(file, stage);
            var antialiasing = Platform.isSupported(ConditionalFeature.EFFECT)
                    ? SceneAntialiasing.BALANCED
                    : SceneAntialiasing.DISABLED;

            Scene scene = new Scene(root, ApplicationWindow.MIN_WIDTH + 80, 768, false, antialiasing);

            Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
            scene.getStylesheets().add(CAD.class.getResource("/mspm/pages/AssetsCSS.css").toString());

            stage.initStyle(StageStyle.DECORATED);
            stage.setResizable(false);

            stage.setHeight(LayoutSizeManager.getResizedHeight(600));
            stage.setWidth(LayoutSizeManager.getResizedWidth(450));

            stage.setScene(scene);

            stage.setTitle("Download");

            stage.showAndWait();
        } catch (Exception e) {
            printNicerStackTrace(e);
        }
    }


}
