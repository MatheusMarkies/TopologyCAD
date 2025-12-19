package com.brasens;

import atlantafx.base.theme.*;
import com.brasens.layout.ApplicationWindow;
import com.brasens.layout.LayoutSizeManager;
import com.brasens.layout.UpdatePopup;
import com.brasens.layout.controller.fxml.ActionStatusPopUp;
import com.brasens.model.Update;
import com.brasens.utilities.common.FilesManager;
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

import java.io.IOException;

import static com.brasens.Config.APP_VERSION;
import static javafx.application.Application.launch;

@Getter @Setter
public class CAD extends Application {
    private Scene scene;
    private Stage stage;

    @Override
    public void start(Stage stage) {
        FilesManager.applicationDirCreator();

        System.out.println("APP_VERSION: "+ APP_VERSION);

        var root = new ApplicationWindow();
        var antialiasing = Platform.isSupported(ConditionalFeature.EFFECT)
                ? SceneAntialiasing.BALANCED
                : SceneAntialiasing.DISABLED;

        scene = new Scene(root, ApplicationWindow.MIN_WIDTH + 80, 768, false, antialiasing);

        Application.setUserAgentStylesheet(new atlantafx.base.theme.PrimerDark().getUserAgentStylesheet());

        scene.getStylesheets().add(getClass().getResource("/mspm/pages/DashboardCSS.css").toString());

        stage.setScene(scene);

        stage.setTitle("Workbench");

        //Image brasensIcon = new Image(Workbench.class.getResource("/mspm/resources/Icone File.png").toString());

        //stage.getIcons().add(brasensIcon);
        stage.setScene(scene);

        stage.setOnCloseRequest(event -> {
            Platform.exit();
            System.exit(0);
        });

        stage.setMaximized(true);
        stage.show();
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

    public static void openUpdatePopUp(Update update) {
        try {
            var root = new UpdatePopup(update);
            var antialiasing = Platform.isSupported(ConditionalFeature.EFFECT)
                    ? SceneAntialiasing.BALANCED
                    : SceneAntialiasing.DISABLED;

            Scene scene = new Scene(root, ApplicationWindow.MIN_WIDTH + 80, 768, false, antialiasing);

            Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());

            scene.getStylesheets().add(CAD.class.getResource("/mspm/pages/DashboardCSS.css").toString());

            Stage stage = new Stage();
            stage.setResizable(false);

            stage.setHeight(LayoutSizeManager.getResizedHeight(600));
            stage.setWidth(LayoutSizeManager.getResizedWidth(450));

            stage.setScene(scene);
            stage.initStyle(StageStyle.UTILITY);
            stage.setTitle("Nova versão disponivel");

            stage.show();

            //PauseTransition delay = new PauseTransition(Duration.seconds(2));
            //delay.setOnFinished( event -> stage.close() );
            //delay.play();
        } catch (Exception e) {
            printNicerStackTrace(e);
        }
    }


}
