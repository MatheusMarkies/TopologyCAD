package com.brasens.layout.view;

import com.brasens.CAD;
import com.brasens.NetworkManager;
import com.brasens.functions.HandleFunctions;
import com.brasens.functions.PointImporter;
import com.brasens.functions.ProjectFileManager;
import com.brasens.layout.ApplicationWindow;
import com.brasens.layout.components.CAD.Canvas.CadCanvas;
import com.brasens.layout.components.CustomButton;
import com.brasens.layout.components.ProjectPropertiesDialog;
import com.brasens.layout.components.PropertiesSidebar;
import com.brasens.layout.components.cells.CoordinateRow;
import com.brasens.layout.controller.DashboardController;
import com.brasens.model.io.ProjectSaveState;
import com.brasens.model.report.ProjectData;
import com.brasens.model.objects.TopoObject;
import com.brasens.model.objects.TopoPoint;
import com.brasens.utils.Page;
import com.brasens.utils.ScaleCalculator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Getter
@Setter
public class DashboardView extends Page {

    private HandleFunctions functions = new HandleFunctions();
    private CadCanvas cadCanvas;

    private VBox topContainer;

    private CustomButton btnImport;
    private CustomButton btnSave;
    private CustomButton btnPan;
    private CustomButton btnLine;
    private CustomButton btnPolyline;
    private CustomButton btnUIText;
    private CustomButton btnZoomExtents;
    private CustomButton btnShowCoordenatesTable;
    BorderPane layout = new BorderPane();

    private VBox sidebarPane;
    private Label lblAreaValue;
    private Label lblPerimeterValue;
    private TableView<CoordinateRow> coordinateTableView;
    private boolean isTableVisible = true;

    private ProjectData projectData;
    private PropertiesSidebar propertiesSidebar;

    public DashboardView(ApplicationWindow applicationWindow, NetworkManager networkManager) {
        super(applicationWindow, networkManager, "/mspm/pages/DashboardCSS.css");
        this.controller = new DashboardController(applicationWindow);
        createView();
    }

    public void createView() {
        getStyleClass().add("body");

        AnchorPane contentAnchorPane = new AnchorPane();
        contentAnchorPane.getStyleClass().add("body");

        layout = new BorderPane();

        setupShortcuts();
        projectData = new ProjectData();
        createSidebar();

        if (isTableVisible) {
            layout.setLeft(sidebarPane);
        } else {
            layout.setLeft(null);
        }

        topContainer = new VBox();

        // 1. Cria o Menu Superior (Texto)
        MenuBar menuBar = createMenuBar();
        ToolBar mainToolBar = createMainToolBar();

        topContainer.getChildren().addAll(menuBar, mainToolBar);

        // Define o topo do layout
        layout.setTop(topContainer);

        propertiesSidebar = new PropertiesSidebar(projectData);
        layout.setRight(propertiesSidebar);

        Pane canvasContainer = new Pane();
        canvasContainer.setStyle("-fx-background-color: #1e1e1e;"); // Fundo escuro do container

        cadCanvas = new CadCanvas(functions);
        canvasContainer.getChildren().add(cadCanvas);

        cadCanvas.widthProperty().bind(canvasContainer.widthProperty());
        cadCanvas.heightProperty().bind(canvasContainer.heightProperty());

        canvasContainer.widthProperty().addListener(o -> cadCanvas.redraw());
        canvasContainer.heightProperty().addListener(o -> cadCanvas.redraw());

        layout.setCenter(canvasContainer);

        layout.prefWidthProperty().bind(contentAnchorPane.widthProperty());
        layout.prefHeightProperty().bind(contentAnchorPane.heightProperty());

        contentAnchorPane.getChildren().add(layout);

        AnchorPane.setBottomAnchor(contentAnchorPane, 0.0);
        AnchorPane.setLeftAnchor(contentAnchorPane, 0.0);
        AnchorPane.setRightAnchor(contentAnchorPane, 0.0);
        AnchorPane.setTopAnchor(contentAnchorPane, 0.0);
        AnchorPane.setBottomAnchor(layout, 0.0);
        AnchorPane.setLeftAnchor(layout, 0.0);
        AnchorPane.setRightAnchor(layout, 0.0);
        AnchorPane.setTopAnchor(layout, 0.0);

        getChildren().add(contentAnchorPane);
    }

    private void setupShortcuts() {
        this.setFocusTraversable(true);

        this.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case ESCAPE -> {
                    if (controller instanceof DashboardController dashCtrl) {

                    }

                    functions.cancelOperation(cadCanvas);
                    deselectAllButtons();
                }
                case DELETE -> {
                }
            }
        });

        this.setOnMouseClicked(e -> this.requestFocus());
    }

    private void deselectAllButtons() {
        btnLine.setActive(false);
        btnPolyline.setActive(false);
        btnUIText.setActive(false);
    }

    Color colorDefault = Color.TRANSPARENT;
    Color colorHover = Color.TRANSPARENT;
    Color colorActive = Color.web("#0078D7");

    private void selectTool(CustomButton selectedBtn, HandleFunctions.FunctionType type) {
        if (selectedBtn.isActive()) {
            selectedBtn.setActive(false);
            functions.setFunction(HandleFunctions.FunctionType.NONE);
        }

        else {
            btnLine.setActive(false);
            btnPolyline.setActive(false);
            btnUIText.setActive(false);
            btnPan.setActive(false);

            selectedBtn.setActive(true);
            functions.setFunction(type);
        }
    }

    private void toggleTable() {
        isTableVisible = !isTableVisible;

        if (isTableVisible) {
            layout.setLeft(sidebarPane);
        } else {
            layout.setLeft(null);
        }

        cadCanvas.redraw();
    }

    private TableView<CoordinateRow> createTable() {
        TableView<CoordinateRow> table = new TableView<>();

        TableColumn<CoordinateRow, String> colDe = new TableColumn<>("De");
        colDe.setCellValueFactory(data -> data.getValue().deProperty());
        colDe.setPrefWidth(50);

        TableColumn<CoordinateRow, String> colPara = new TableColumn<>("Para");
        colPara.setCellValueFactory(data -> data.getValue().paraProperty());
        colPara.setPrefWidth(50);

        TableColumn<CoordinateRow, String> colN = new TableColumn<>("Norte (Y)");
        colN.setCellValueFactory(data -> data.getValue().coordNProperty());
        colN.setPrefWidth(80);

        TableColumn<CoordinateRow, String> colE = new TableColumn<>("Este (X)");
        colE.setCellValueFactory(data -> data.getValue().coordEProperty());
        colE.setPrefWidth(80);

        TableColumn<CoordinateRow, String> colDist = new TableColumn<>("Distância");
        colDist.setCellValueFactory(data -> data.getValue().distanciaProperty());
        colDist.setPrefWidth(80);

        table.getColumns().addAll(colDe, colPara, colN, colE, colDist);
        return table;
    }

    private void createSidebar() {
        sidebarPane = new VBox();

        sidebarPane.setStyle("-fx-background-color: #1e1e1e; -fx-border-color: #3f3f3f; -fx-border-width: 0 0 0 1;");
        sidebarPane.setPrefWidth(350);
        sidebarPane.setPadding(new Insets(15)); // Offset geral (Top, Right, Bottom, Left)
        sidebarPane.setSpacing(15); // Espaço entre Tabela e Totais

        coordinateTableView = createTable();
        VBox.setVgrow(coordinateTableView, Priority.ALWAYS);

        VBox statsCard = createStatsCard();
        sidebarPane.getChildren().addAll(coordinateTableView, statsCard);
    }

    private VBox createStatsCard() {
        VBox card = new VBox();
        card.setPadding(new Insets(15));
        card.setSpacing(5);

        card.setStyle("-fx-background-color: #2b2b2b; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 2);");

        Label lblTitle = new Label("Resumo do Perímetro");
        lblTitle.setTextFill(Color.web("#888888"));
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));

        Label lblAreaTitle = new Label("Área Total:");
        lblAreaTitle.setTextFill(Color.WHITE);

        lblAreaValue = new Label("0.0000 ha");
        lblAreaValue.setTextFill(Color.web("#0078D7")); // Azul destaque
        lblAreaValue.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));

        Label lblPerimTitle = new Label("Perímetro:");
        lblPerimTitle.setTextFill(Color.WHITE);

        lblPerimeterValue = new Label("0.00 m");
        lblPerimeterValue.setTextFill(Color.web("#0078D7"));
        lblPerimeterValue.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));

        card.getChildren().addAll(
                lblTitle,
                new Separator(),
                lblAreaTitle, lblAreaValue,
                lblPerimTitle, lblPerimeterValue
        );

        return card;
    }

    public void updateCoordinatesTable(List<TopoPoint> points) {
        ObservableList<CoordinateRow> rows = FXCollections.observableArrayList();

        if (points.size() < 2) {
            coordinateTableView.setItems(rows);
            lblAreaValue.setText("0.0000 ha");
            lblPerimeterValue.setText("0.00 m");
            return;
        }

        for (int i = 0; i < points.size(); i++) {
            TopoPoint current = points.get(i);
            TopoPoint next = points.get((i + 1) % points.size());
            double dist = Math.hypot(next.getX() - current.getX(), next.getY() - current.getY());

            rows.add(new CoordinateRow(current.getName(), next.getName(), current.getY(), current.getX(), dist));
        }
        coordinateTableView.setItems(rows);

        TopoObject tempPoly = new TopoObject(points, true);

        double areaHa = tempPoly.getAreaHa();
        double perimeterM = tempPoly.getPerimeter();

        lblAreaValue.setText(String.format("%.4f ha", areaHa));
        lblPerimeterValue.setText(String.format("%.2f m", perimeterM));

        if (propertiesSidebar != null) {
            propertiesSidebar.updateMetrics(areaHa, perimeterM);
        }

        double bestScale = ScaleCalculator.calculateBestScale(tempPoly, ScaleCalculator.PaperSize.A4);

        String scaleText = String.format("1 / %.0f", bestScale);

        if (this.projectData != null) {
            this.projectData.getTechnicalSpecs().setEscalaTexto(scaleText);
        }

        if (propertiesSidebar != null) {
            propertiesSidebar.updateMetrics(areaHa, perimeterM);
            propertiesSidebar.refreshData();
        }

        if (!isTableVisible) toggleTable();
    }

    private void switchToolBar(String type) {
        if (topContainer.getChildren().size() > 1) {
            topContainer.getChildren().remove(1);
        }

        ToolBar newBar;
        switch (type) {
            case "MAP10X" -> newBar = createMap10XToolBar();
            case "MAIN" -> newBar = createMainToolBar(); // Sua antiga createCommandBar
            default -> newBar = createMainToolBar();
        }

        topContainer.getChildren().add(newBar);
    }

    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();

        menuBar.setStyle("-fx-background-color: #2b2b2b; -fx-padding: 0; -fx-background-insets: 0;");
        menuBar.getStyleClass().add("dark-menu-bar"); // Classe para o CSS

        Menu menuFile = new Menu("Arquivo");

        MenuItem itemOpen = new MenuItem("Abrir Projeto");
        itemOpen.setOnAction(e -> openProject());

        MenuItem itemSaveProject = new MenuItem("Salvar Projeto");
        itemSaveProject.setOnAction(e -> saveProject());

        MenuItem itemImport = new MenuItem("Importar Pontos");
        itemImport.setOnAction(e -> btnImport.getOnMouseClicked().handle(null));

        MenuItem itemExit = new MenuItem("Sair");
        itemExit.setOnAction(e -> System.exit(0));

        menuFile.getItems().addAll(itemOpen, itemSaveProject, new SeparatorMenuItem(), itemImport, new SeparatorMenuItem(), itemExit);

        Menu menuTools = new Menu("Modos");
        ToggleGroup modeGroup = new ToggleGroup();

        RadioMenuItem modeMain = new RadioMenuItem("CAD Principal");
        modeMain.setToggleGroup(modeGroup);
        modeMain.setSelected(true);
        modeMain.setOnAction(e -> switchToolBar("MAIN"));

        RadioMenuItem modeMap10X = new RadioMenuItem("Mapa 10X");
        modeMap10X.setToggleGroup(modeGroup);
        modeMap10X.setOnAction(e -> switchToolBar("MAP10X"));

        menuTools.getItems().addAll(modeMain, modeMap10X);

        Menu menuView = new Menu("Exibir");

        Menu menuProps = new Menu("Propriedades");
        MenuItem itemEditProject = new MenuItem("Editar Informações do Projeto");
        itemEditProject.setOnAction(e -> openProjectPropertiesDialog());

        MenuItem itemLoadImage = new MenuItem("Definir Imagem de Localização");
        itemLoadImage.setOnAction(e -> loadLocationImage());

        menuProps.getItems().addAll(itemEditProject, new SeparatorMenuItem(), itemLoadImage);

        CheckMenuItem itemToggleTable = new CheckMenuItem("Painel Lateral");
        itemToggleTable.setSelected(isTableVisible);
        itemToggleTable.setOnAction(e -> {
            toggleTable();

            itemToggleTable.setSelected(isTableVisible);
            btnShowCoordenatesTable.setActive(!btnShowCoordenatesTable.isActive());
        });

        menuView.getItems().add(itemToggleTable);

        menuBar.getMenus().addAll(menuFile, menuTools, menuProps, menuView);
        return menuBar;
    }

    private void saveProject() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Salvar Projeto CAD");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivo de Mapa (*.map)", "*.map"));

        fileChooser.setInitialFileName("projeto_novo.map");

        File file = fileChooser.showSaveDialog(getScene().getWindow());

        if (file != null) {
            try {
                List<TopoObject> mapObjects = cadCanvas.getObjects();

                ProjectSaveState state = new ProjectSaveState(this.projectData, mapObjects);

                ProjectFileManager.saveProject(file, state);

                System.out.println("Projeto salvo com sucesso em: " + file.getAbsolutePath());

            } catch (Exception ex) {
                ex.printStackTrace();
                showAlert("Erro ao Salvar", "Não foi possível salvar o arquivo: " + ex.getMessage());
            }
        }
    }

    private void openProject() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Abrir Projeto CAD");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivo de Mapa (*.map)", "*.map"));

        File file = fileChooser.showOpenDialog(getScene().getWindow());

        if (file != null) {
            try {
                ProjectSaveState state = ProjectFileManager.loadProject(file);

                if (state != null) {
                    this.projectData = state.getProjectData();
                    updateSidebarWithNewData();

                    List<TopoObject> loadedObjects = state.getMapObjects();
                    cadCanvas.setObjects(loadedObjects);
                    cadCanvas.zoomExtents();
                    cadCanvas.redraw();

                    if (loadedObjects != null && !loadedObjects.isEmpty()) {
                        for(TopoObject obj : loadedObjects)
                            if (obj.getLayerName().equals("PERIMETRO")){
                                updateCoordinatesTable(obj.getPoints());
                                break;
                            }

                    } else {
                        updateCoordinatesTable(new ArrayList<>());
                    }

                    System.out.println("Projeto carregado com sucesso!");
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                showAlert("Erro ao Abrir", "Arquivo corrompido ou incompatível: " + ex.getMessage());
            }
        }
    }

    private void updateSidebarWithNewData() {
        layout.getChildren().remove(propertiesSidebar);
        propertiesSidebar = new PropertiesSidebar(this.projectData);
        if (isTableVisible) {
            layout.setRight(propertiesSidebar);
        }

    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void openProjectPropertiesDialog() {
        ProjectPropertiesDialog dialog = new ProjectPropertiesDialog(projectData);

        Optional<Boolean> result = dialog.showAndWait();

        if (result.isPresent() && result.get()) {
            System.out.println("Dados do projeto atualizados.");

            if (propertiesSidebar != null) {
                propertiesSidebar.refreshData();
            }
        }
    }

    private void loadLocationImage() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Selecione a Planta de Localização");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Imagens", "*.png", "*.jpg", "*.jpeg"));
        File file = fc.showOpenDialog(getScene().getWindow());

        if (file != null) {
            Image img = new Image(file.toURI().toString());
            projectData.setPlantaLocalizacao(img);
            if (propertiesSidebar != null) propertiesSidebar.refreshData();
        }
    }

    private ToolBar createMap10XToolBar() {
        ToolBar toolBar = new ToolBar();

        double height = 45.0;
        toolBar.setPrefHeight(height);
        toolBar.setMinHeight(height);
        toolBar.setMaxHeight(height);

        toolBar.setPadding(new javafx.geometry.Insets(5, 10, 5, 10));
        toolBar.setStyle("-fx-base: #2b2b2b; -fx-background-color: #2b2b2b;");

        int btnImageSize = 18;
        Label lblMode = new Label("MAPA 10X");
        lblMode.setTextFill(Color.web("#888888"));
        lblMode.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));

        CustomButton btnMapPan = new CustomButton("",
                new Image(CAD.class.getResource("/mspm/icons/move.png").toString()),
                "",
                btnImageSize
        );
        btnMapPan.setAnimation(colorDefault, colorHover, colorActive, 200, true);
        btnMapPan.setOnMouseClicked(e -> {
            btnMapPan.setActive(true);
            functions.setFunction(HandleFunctions.FunctionType.EDGEPAN);
            functions.setEdgePanEnabled(true);
        });
        btnMapPan.setActive(true);
        functions.setFunction(HandleFunctions.FunctionType.EDGEPAN);

        // Botão Zoom Extents
        CustomButton btnMapZoom = new CustomButton("",
                new Image(CAD.class.getResource("/mspm/icons/zoom-in.png").toString()),
                "",
                btnImageSize
        );
        btnMapZoom.setOnMouseClicked(e -> getCadCanvas().zoomExtents());

        CustomButton btnLayers = new CustomButton("",
                new Image(CAD.class.getResource("/mspm/icons/table-grid.png").toString()),
                "",
                btnImageSize
        );
        btnLayers.setAnimation(colorDefault, colorHover, colorActive, 200, true);
        btnLayers.setOnMouseClicked(e -> {
            System.out.println("Abrir gerenciador de camadas do Mapa 10X...");
            btnLayers.setActive(!btnLayers.isActive());
        });

        // --- 3. ADICIONA TUDO NA BARRA ---
        toolBar.getItems().addAll(
                lblMode,
                new Separator(Orientation.VERTICAL),
                btnMapPan,
                btnMapZoom,
                new Separator(Orientation.VERTICAL),
                btnLayers
                // Adicione mais ferramentas específicas do Mapa 10X aqui
        );

        return toolBar;
    }

    private ToolBar createMainToolBar() {
        ToolBar toolBar = new ToolBar();

        double height = 45.0;
        toolBar.setPrefHeight(height);
        toolBar.setMinHeight(height);
        toolBar.setMaxHeight(height);

        toolBar.setPadding(new javafx.geometry.Insets(5, 10, 5, 10));

        Color colorDefault = Color.TRANSPARENT;
        Color colorHover = Color.web("#3f3f3f"); // Um cinza levemente mais claro para hover
        Color colorActive = Color.web("#0078D7"); // Azul para ativo

        int btnImageSize = 18;

        // --- BOTÃO IMPORTAR ---
        btnImport = new CustomButton("",
                new Image(CAD.class.getResource("/mspm/icons/import.png").toString()),
                "",//
                btnImageSize
        );

        btnImport.setAnimation(colorDefault, colorHover, colorActive, 200, false);
        btnImport.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Importar Pontos Topográficos");
                fileChooser.getExtensionFilters().add(
                        new FileChooser.ExtensionFilter("Arquivos de Texto", "*.txt", "*.csv")
                );

                File file = fileChooser.showOpenDialog(getScene().getWindow());

                if (file != null) {
                    try {
                        List<TopoPoint> pontos = PointImporter.importFromCSV(file);
                        System.out.println("Importados " + pontos.size() + " pontos.");

                        List<TopoPoint> importedPoints = PointImporter.importFromCSV(file);
                        updateCoordinatesTable(importedPoints);

                        getCadCanvas().setImportedPoints(pontos);

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        // --- BOTÃO SALVAR ---
        btnSave = new CustomButton("",
                new Image(CAD.class.getResource("/mspm/icons/save.png").toString()),
                "",
                btnImageSize
        );

        btnSave.setAnimation(colorDefault, colorHover, colorActive, 200, false);
        btnSave.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                saveProject();
            }
        });

        // --- BOTÃO ZOOM EXTENTS ---
        btnZoomExtents = new CustomButton("",
                new Image(CAD.class.getResource("/mspm/icons/zoom-in.png").toString()),
                "",
                btnImageSize
        );

        btnZoomExtents.setAnimation(colorDefault, colorHover, colorActive, 200, false);
        btnZoomExtents.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                getCadCanvas().zoomExtents();
            }
        });

        // --- BOTÃO PAN (Este é Toggle = true) ---
        btnPan = new CustomButton("",
                new Image(CAD.class.getResource("/mspm/icons/move.png").toString()),
                "",
                btnImageSize
        );
        btnPan.setAnimation(colorDefault, colorHover, colorActive, 200, true);
        btnPan.setOnMouseClicked(e -> {
            selectTool(btnPan, HandleFunctions.FunctionType.EDGEPAN);
            functions.setEdgePanEnabled(!functions.isEdgePanEnabled());
        });

        // --- FERRAMENTAS DE DESENHO (São Toggle = true) ---

        btnLine = new CustomButton("",
                new Image(CAD.class.getResource("/mspm/icons/nodes.png").toString()),
                "",
                btnImageSize
        );
        btnLine.setAnimation(colorDefault, colorHover, colorActive, 200, true);
        btnLine.setOnMouseClicked(e -> {
            selectTool(btnLine, HandleFunctions.FunctionType.LINE);
        });

        btnPolyline = new CustomButton("",
                new Image(CAD.class.getResource("/mspm/icons/polyline.png").toString()),
                "",
                btnImageSize
        );
        btnPolyline.setAnimation(colorDefault, colorHover, colorActive, 200, true);
        btnPolyline.setOnMouseClicked(e -> {
            selectTool(btnPolyline, HandleFunctions.FunctionType.POLYLINE);
        });

        btnUIText = new CustomButton("",
                new Image(CAD.class.getResource("/mspm/icons/text.png").toString()),
                "",
                btnImageSize
        );
        btnUIText.setAnimation(colorDefault, colorHover, colorActive, 200, true);
        btnUIText.setOnMouseClicked(e -> {
            selectTool(btnUIText, HandleFunctions.FunctionType.TEXT);
        });

        // --- BOTÃO TABELA ---
        btnShowCoordenatesTable = new CustomButton("",
                new Image(CAD.class.getResource("/mspm/icons/table-grid.png").toString()),
                "",
                btnImageSize
        );
        btnShowCoordenatesTable.setAnimation(colorDefault, colorHover, colorActive, 200, true);
        btnShowCoordenatesTable.setOnMouseClicked(e -> {
            toggleTable();
            btnShowCoordenatesTable.setActive(!btnShowCoordenatesTable.isActive());
        });
        btnShowCoordenatesTable.setActive(true);

        // Adiciona tudo na barra com separadores
        toolBar.getItems().addAll(
                btnImport,
                btnSave,
                new Separator(Orientation.VERTICAL),
                btnPan,
                btnZoomExtents, // Zoom fica perto do Pan por lógica
                new Separator(Orientation.VERTICAL),
                btnLine,
                btnPolyline,
                btnUIText,
                new Separator(Orientation.VERTICAL),
                btnShowCoordenatesTable
        );

        return toolBar;
    }
}