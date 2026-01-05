package com.brasens.layout;

import com.brasens.Config;
import com.brasens.NetworkManager;
import com.brasens.model.Update;
import com.brasens.repository.UpdateRepository;
import com.brasens.utilities.FileDownload;
import com.brasens.utilities.common.FilesManager;
import com.brasens.utils.NodeUtils;
import com.brasens.utils.Page;
import jakarta.annotation.PostConstruct;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * This class, ApplicationWindow, represents the main plan of a graphical interface.
 * It extends JavaFX AnchorPane class and is responsible for managing the program's sub-interfaces.
 * The sub-interfaces are represented by classes that extend the Page class, which is also an extension of AnchorPane.
 * The organization of the sub-interfaces follows the MVC (Model View Controller) pattern.
 * The classes with the View function are the sub-interfaces, which extend the Page class and control the visual presentation.
 * The classes that perform the Model function extend the Controller class and manage the business logic and data.
 * The ApplicationWindow class acts as the application's main Controller.
 * -------------------------------------------------------------------------------------------------------------------------------------
 * The code includes methods for initialization, switching pages and logging in, following the MVC logic.
 * The init() method can be customized for specific application initialization.
 * The changePage() method manages the exchange between different sub-interfaces, initializing and updating their controllers.
 * The realiseLogin() method configures the interface after login, displaying the navigation bar and the top menu.
 * The ViewManager class has the function of storing the sub-interfaces that have already been initialized, as well as the main setup with navbar and topmenu.
 *-------------------------------------------------------------------------------------------------------------------------------------
 * The class uses NetworkManager and ViewManager objects to handle network operations and manage the sub-interfaces, respectively.
 * The layout structure is based on an AnchorPane containing a BorderPane, with the sub-interfaces being displayed in the center.
 * As well as the navbar on the left side of the BorderPane and the TopMenu at the top of it.
 * The interface body is identified using the BODY_ID constant.
 *-------------------------------------------------------------------------------------------------------------------------------------
 * Note: The MVC design pattern is adopted to maintain a clear separation between presentation logic, business logic and data.
 *
 * com.brasens.javafx.utils.Page
 * com.brasens.javafx.utils.Controller
 * com.brasens.layout.components.navbar.NavBar
 * com.brasens.layout.components.TopMenu
 * com.brasens.layout.ViewManager
 *
 */

@Component
@Getter @Setter
public class ApplicationWindow extends AnchorPane {

    // Main class
    // ATENÇÃO: Evite nomear variáveis igual à Classe (CAD CAD), isso causa confusão e possíveis erros.
    // Sugestão: private CAD mainApp;
    com.brasens.CAD CAD;

    @Autowired
    private UpdateRepository updateRepository;

    public static final String BODY_ID = "body";
    public static final int MIN_WIDTH = 1280;

    private NetworkManager networkManager;
    private ViewManager viewManager;

    BorderPane borderPane = new BorderPane();

    // Class constructor
    public ApplicationWindow() {
        // Starting network managers and visualization
        networkManager = new NetworkManager();
        viewManager = new ViewManager(this, networkManager);

        // Interface configuration
        viewManager.setup(this);

        // --- CORREÇÃO: init() REMOVIDO DAQUI ---
        // O init() agora será chamado automaticamente pelo Spring após a injeção do Repository

        // Configuring the size, style and identification of the main window
        setPrefSize(LayoutSizeManager.getResizedWidth(1280), LayoutSizeManager.getResizedHeight(800));
        getStyleClass().add("body");
        setId(BODY_ID);

        // Setting up anchors for the layout
        NodeUtils.setAnchors(this, Insets.EMPTY);
        AnchorPane.setBottomAnchor(borderPane, 0.0);
        AnchorPane.setLeftAnchor(borderPane, 0.0);
        AnchorPane.setRightAnchor(borderPane, 0.0);
        AnchorPane.setTopAnchor(borderPane, 0.0);

        // Style settings for the body of the interface
        borderPane.getStyleClass().add("body");

        // Initially loads the login page
        changePage(viewManager.getDashboardView());
        BorderPane.setAlignment(viewManager.getDashboardView(), Pos.CENTER);

        // Adds BorderPane as a child of AnchorPane (main window)
        getChildren().addAll(borderPane);
    }

    @PostConstruct
    public void init(){
        tryUpdate();
    }

    public void tryUpdate() {
        // Verificação de segurança
        if (updateRepository == null) return;

        // 1. Acesso ao Banco de Dados (Pode rodar na thread do Spring)
        List<Update> updateList = updateRepository.findAll();
        Update appUpdate = new Update();

        if (!updateList.isEmpty())
            appUpdate = updateList.get(0);

        System.out.println("Config.APP_VERSION: " + Config.APP_VERSION);
        System.out.println("appUpdate.APP_VERSION: " + appUpdate.getVersion());

        if (appUpdate.getVersion() != null && !appUpdate.getVersion().equals(Config.APP_VERSION)) {

            FileDownload fileDownload = new FileDownload("TopologyCAD.exe", FilesManager.ApplicationDataFolder + "\\update", appUpdate.getURL());
            System.out.println("Open Update Popup!");

            Platform.runLater(() -> {
                try {
                    // Tenta usar a instância injetada ou o método estático
                    if (CAD != null) {
                        CAD.openDownloadPopUp(fileDownload);
                    } else {

                        com.brasens.CAD.openDownloadPopUp(fileDownload);
                    }
                } catch (Exception e) {
                    com.brasens.CAD.printNicerStackTrace(e);
                }
            });
        }
    }

    // ... (restante do código: changePage, etc) ...
    private Page currentPageLoaded;

    public void changePage(Page page){
        borderPane.setCenter(page);
        page.getController().init();

        if(currentPageLoaded != null) {
            currentPageLoaded.getController().close();
            currentPageLoaded.getController().setUpdate(false);
        }

        page.getController().setUpdate(true);
        currentPageLoaded = page;
    }
}