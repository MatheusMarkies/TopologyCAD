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
import com.brasens.model.objects.TopoObject;
import com.brasens.model.objects.TopoPoint;
import com.brasens.model.report.ProjectData;
import com.brasens.utilities.common.SheetManager;
import com.brasens.utilities.math.ContourGenerator;
import com.brasens.utilities.math.CoordinateConversion;
import com.brasens.utilities.math.ScaleCalculator;
import com.brasens.utilities.math.TopologyMath;
import com.brasens.utils.Page;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Side;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.util.Pair;
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

    /* CAD */
    private CustomButton btnImport;
    private CustomButton btnSave;
    private CustomButton btnPan;
    private CustomButton btnLine;
    private CustomButton btnPolyline;
    private CustomButton btnJoin;
    private CustomButton btnUIText;

    private CustomButton btnToggleGrid;

    private CustomButton btnZoomExtents;
    private CustomButton btnShowCoordinatesTable;
    private CustomButton btnLayers;

    private CustomButton btnDimArea;
    private CustomButton btnDimSegments;
    private CustomButton btnDimAngle;
    /* CAD */

    /* MAPA 10 X */
    private CustomButton btnTable;

    private CustomButton btnContour;

    private CustomButton btnDivideArea;

    private CustomButton btnConfrontante;

    private CustomButton btnInsertSheet;
    private CustomButton btnConfigVertices;

    private CustomButton btnMemorial;
    private CustomButton btnExportMap;
    /* MAPA 10 X */

    BorderPane layout = new BorderPane();

    private VBox sidebarPane;
    private Label lblAreaValue;
    private Label lblPerimeterValue;
    private TableView<CoordinateRow> coordinateTableView;
    private boolean isTableVisible = true;

    private ProjectData projectData;
    private PropertiesSidebar propertiesSidebar;

    private VBox infoOverlay;
    private Label lblInfoName;
    private Label lblInfoX;
    private Label lblInfoY;
    private Label lblInfoZ;
    private boolean isMap10XMode = false;

    private Label lblToolTip;
    private StackPane tipsContainer;

    Color colorDefault = Color.TRANSPARENT;
    Color colorHover = Color.TRANSPARENT;
    Color colorActive = Color.web("#0078D7");

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
        createInfoOverlay();
        createTipsOverlay();

        canvasContainer.getChildren().addAll(cadCanvas, infoOverlay, tipsContainer);

        tipsContainer.layoutXProperty().bind(canvasContainer.widthProperty().subtract(tipsContainer.widthProperty()).divide(2));
        tipsContainer.layoutYProperty().bind(canvasContainer.heightProperty().subtract(tipsContainer.heightProperty()));

        cadCanvas.widthProperty().bind(canvasContainer.widthProperty());
        cadCanvas.heightProperty().bind(canvasContainer.heightProperty());

        canvasContainer.widthProperty().addListener(o -> cadCanvas.redraw());
        canvasContainer.heightProperty().addListener(o -> cadCanvas.redraw());

        cadCanvas.setOnSelectionChanged(point -> {
            handleSelectionUpdate(point);
        });
        cadCanvas.setOnContentChange(() -> {
            handleSelectionUpdate(null);
        });

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

    private void createInfoOverlay() {
        infoOverlay = new VBox(5); // Espaçamento 5px
        infoOverlay.setPadding(new Insets(10));

        // Estilo "HUD": Fundo escuro semitransparente, bordas arredondadas
        infoOverlay.setStyle(
                "-fx-background-color: rgba(40, 40, 40, 0.85);" +
                        "-fx-background-radius: 8;" +
                        "-fx-border-color: #555;" +
                        "-fx-border-radius: 8;" +
                        "-fx-border-width: 1;"
        );

        // Sombra para destacar do fundo
        infoOverlay.setEffect(new DropShadow(10, Color.BLACK));

        // Posicionamento no canto superior esquerdo (dentro do Pane)
        infoOverlay.setLayoutX(15);
        infoOverlay.setLayoutY(15);

        // Inicialmente invisível
        infoOverlay.setVisible(false);
        // Não captura cliques (para não atrapalhar o desenho se passar mouse por cima)
        infoOverlay.setMouseTransparent(true);

        // Labels
        lblInfoName = createInfoLabel("", FontWeight.BOLD, Color.WHITE);
        lblInfoX = createInfoLabel("", FontWeight.NORMAL, Color.LIGHTGRAY);
        lblInfoY = createInfoLabel("", FontWeight.NORMAL, Color.LIGHTGRAY);
        lblInfoZ = createInfoLabel("", FontWeight.NORMAL, Color.LIGHTGRAY);

        infoOverlay.getChildren().addAll(lblInfoName, new Separator(), lblInfoX, lblInfoY, lblInfoZ);
    }

    private Label createInfoLabel(String text, FontWeight weight, Color color) {
        Label lbl = new Label(text);
        lbl.setFont(Font.font("Consolas", weight, 12)); // Fonte monoespaçada alinha melhor números
        lbl.setTextFill(color);
        return lbl;
    }

    private void updateInfoOverlay(TopoPoint p) {
        lblInfoName.setText("Ponto: " + p.getName());
        lblInfoX.setText(String.format("E (X): %.3f m", p.getX()));
        lblInfoY.setText(String.format("N (Y): %.3f m", p.getY()));
        lblInfoZ.setText(String.format("Alt(Z): %.3f m", p.getZ()));
    }

    private void setupShortcuts() {
        this.setFocusTraversable(true);

        this.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case ESCAPE -> {
                    functions.cancelOperation(cadCanvas);
                    deselectAllButtons();
                }
                case DELETE -> {
                    cadCanvas.deleteSelected();
                    updateCoordinatesTable(cadCanvas.getSurveyPoints());
                }
                case T -> {
                    editSelectedText();
                }

                case F -> {
                    if (isMap10XMode) {
                        switchToolBar("MAIN");
                        System.out.println("Modo: CAD Principal");
                    } else {
                        switchToolBar("MAP10X");
                        System.out.println("Modo: Mapa 10X");
                    }
                }
            }
        });

        this.setOnMouseClicked(e -> this.requestFocus());
    }

    private void editSelectedText() {
        TopoPoint textPoint = cadCanvas.getSingleSelectedTextPoint();

        if (textPoint == null) {
            return;
        }

        TextInputDialog dialog = new TextInputDialog(textPoint.getName());
        dialog.setTitle("Editar Texto");
        dialog.setHeaderText("Editar conteúdo do texto selecionado");
        dialog.setContentText("Novo texto:");

        dialog.getDialogPane().lookupButton(ButtonType.OK).disableProperty().bind(
                dialog.getEditor().textProperty().isEmpty()
        );

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(newText -> {
            if (!newText.isEmpty()) {
                textPoint.setName(newText);
                cadCanvas.redraw();

                System.out.println("Texto alterado para: " + newText);
            }
        });
    }

    private void deselectAllButtons() {
        // Ferramentas de Visualização
        if (btnPan != null) btnPan.setActive(false);

        // Ferramentas de Desenho (CAD)
        if (btnLine != null) btnLine.setActive(false);
        if (btnPolyline != null) btnPolyline.setActive(false);
        if (btnUIText != null) btnUIText.setActive(false);

        // Ferramentas de Medição
        if (btnDimArea != null) btnDimArea.setActive(false);
        if (btnDimSegments != null) btnDimSegments.setActive(false);
        if (btnDimAngle != null) btnDimAngle.setActive(false);

        // Ferramentas Mapa 10X
        if (btnTable != null) btnTable.setActive(false);
        if (btnConfrontante != null) btnConfrontante.setActive(false);
        if (btnInsertSheet != null) btnInsertSheet.setActive(false);
        if (btnConfigVertices != null) btnConfigVertices.setActive(false);

        // Ferramentas de Ação que podem ter ficado visivelmente ativas
        if (btnDivideArea != null) btnDivideArea.setActive(false);

        updateToolTip("");
    }

    private String getTipForFunction(HandleFunctions.FunctionType type) {
        return switch (type) {
            case LINE -> "Clique no ponto inicial e depois no ponto final.";
            case POLYLINE -> "Clique para adicionar pontos. (Feche no início ou clique duplo para terminar)";
            case TEXT -> "Clique no local desejado para inserir o texto.";
            case PLACE_TABLE -> "Clique na tela para posicionar a tabela.";
            case DIMENSION_AREA -> "Clique dentro ou na borda de um polígono fechado.";
            case DIMENSION_SEGMENTS -> "Clique em uma linha ou segmento para cotar.";
            case DIMENSION_ANGLE -> "Clique no Vértice, depois no Ponto 1 e Ponto 2.";
            case DEFINE_CONFRONTANTE -> "Clique no Ponto A e depois no Ponto B do limite.";
            case PLACE_SHEET -> "Posicione a folha e clique para confirmar.";
            case CONFIG_VERTICES -> "Clique exatamente sobre um vértice para configurar.";
            case EDGEPAN -> "Arraste a tela para mover a visualização.";
            case MOVE_OBJECT -> "Clique no ponto base e arraste para mover o objeto.";
            default -> "";
        };
    }

    private void selectTool(CustomButton selectedBtn, HandleFunctions.FunctionType type) {
        if (selectedBtn.isActive()) {
            selectedBtn.setActive(false);
            functions.setFunction(HandleFunctions.FunctionType.NONE);

            updateToolTip(""); // <--- LIMPA A DICA

            // Reseta cursor se necessário
            if (cadCanvas.getScene() != null) cadCanvas.getScene().setCursor(Cursor.DEFAULT);
        } else {
            // Desativa outros botões (lógica existente...)
            btnLine.setActive(false);
            btnPolyline.setActive(false);
            btnUIText.setActive(false);
            btnPan.setActive(false);
            // (Adicione os outros botões do Mapa10X aqui se necessário desativar todos globalmente)
            // ...

            selectedBtn.setActive(true);
            functions.setFunction(type);

            updateToolTip(getTipForFunction(type)); // <--- MOSTRA A DICA
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

        // --- Colunas Originais ---
        TableColumn<CoordinateRow, String> colDe = new TableColumn<>("De");
        colDe.setCellValueFactory(data -> data.getValue().deProperty());
        colDe.setPrefWidth(40);

        TableColumn<CoordinateRow, String> colPara = new TableColumn<>("Para");
        colPara.setCellValueFactory(data -> data.getValue().paraProperty());
        colPara.setPrefWidth(40);

        TableColumn<CoordinateRow, String> colN = new TableColumn<>("Norte (Y)");
        colN.setCellValueFactory(data -> data.getValue().coordNProperty());
        colN.setPrefWidth(85);

        TableColumn<CoordinateRow, String> colE = new TableColumn<>("Este (X)");
        colE.setCellValueFactory(data -> data.getValue().coordEProperty());
        colE.setPrefWidth(85);

        // Altitude (Z)
        TableColumn<CoordinateRow, String> colZ = new TableColumn<>("Cota Z");
        colZ.setCellValueFactory(data -> data.getValue().coordZProperty());
        colZ.setPrefWidth(60);

        // Distância
        TableColumn<CoordinateRow, String> colDist = new TableColumn<>("Distância");
        colDist.setCellValueFactory(data -> data.getValue().distanciaProperty());
        colDist.setPrefWidth(70);

        // Azimute
        TableColumn<CoordinateRow, String> colAz = new TableColumn<>("Azimute");
        colAz.setCellValueFactory(data -> data.getValue().azimuteProperty());
        colAz.setPrefWidth(90); // Mais largo para caber graus/min/seg

        // Rumo
        TableColumn<CoordinateRow, String> colRumo = new TableColumn<>("Rumo");
        colRumo.setCellValueFactory(data -> data.getValue().rumoProperty());
        colRumo.setPrefWidth(80);

        // Latitude
        TableColumn<CoordinateRow, String> colLat = new TableColumn<>("Latitude");
        colLat.setCellValueFactory(data -> data.getValue().latitudeProperty());
        colLat.setPrefWidth(90);

        // Longitude
        TableColumn<CoordinateRow, String> colLon = new TableColumn<>("Longitude");
        colLon.setCellValueFactory(data -> data.getValue().longitudeProperty());
        colLon.setPrefWidth(90);

        table.getColumns().addAll(
                colDe, colPara,
                colN, colE, colZ,
                colDist, colAz, colRumo,
                colLat, colLon
        );

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

    private void autoResizeColumns(TableView<?> table) {
        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        table.getColumns().forEach(column -> {
            Text t = new Text(column.getText());
            double max = t.getLayoutBounds().getWidth();

            int limit = Math.min(table.getItems().size(), 500);

            for (int i = 0; i < limit; i++) {
                if (column.getCellData(i) != null) {
                    t = new Text(column.getCellData(i).toString());
                    double calcwidth = t.getLayoutBounds().getWidth();
                    if (calcwidth > max) {
                        max = calcwidth;
                    }
                }
            }
            column.setPrefWidth(max + 15.0d);
        });
    }

    public void updateCoordinatesTable(List<TopoPoint> points) {
        ObservableList<CoordinateRow> rows = FXCollections.observableArrayList();

        // Se tiver menos de 2 pontos, limpa tudo
        if (points == null || points.size() < 2) {
            coordinateTableView.setItems(rows);
            lblAreaValue.setText("0.0000 ha");
            lblPerimeterValue.setText("0.00 m");
            return;
        }

        int zonaUtm = projectData.getTechnicalSpecs().getZonaUTM();
        boolean isSul = projectData.getTechnicalSpecs().isHemisferioSul();

        for (int i = 0; i < points.size(); i++) {
            TopoPoint current = points.get(i);
            TopoPoint next = points.get((i + 1) % points.size());

            double dist = TopologyMath.getDistance2D(current, next);
            double azimuteVal = TopologyMath.getAzimuth(current, next);

            String azimuteStr = TopologyMath.degreesToDMS(azimuteVal);
            String rumoStr = TopologyMath.getRumo(azimuteVal);

            double[] latLon = CoordinateConversion.utmToLatLon(current.getX(), current.getY(), zonaUtm, isSul);

            String latFormatada = TopologyMath.formatLatitude(latLon[0]);
            String lonFormatada = TopologyMath.formatLongitude(latLon[1]);

            current.setLatitude(latLon[0]);
            current.setLongitude(latLon[1]);

            rows.add(new CoordinateRow(
                    current.getName(),
                    next.getName(),
                    current.getY(),
                    current.getX(),
                    current.getZ(),
                    dist,
                    azimuteStr,
                    rumoStr,
                    latFormatada,
                    lonFormatada
            ));
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
            propertiesSidebar.refreshData();
        }

        autoResizeColumns(coordinateTableView);
        if (!isTableVisible) toggleTable();
    }

    private void switchToolBar(String type) {
        if (topContainer.getChildren().size() > 1) {
            topContainer.getChildren().remove(1);
        }

        ToolBar newBar;
        switch (type) {
            case "MAP10X" -> {
                newBar = createMap10XToolBar();
                isMap10XMode = true; // Sincroniza estado
            }
            case "MAIN" -> {
                newBar = createMainToolBar();
                isMap10XMode = false; // Sincroniza estado
            }
            default -> {
                newBar = createMainToolBar();
                isMap10XMode = false;
            }
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
            btnShowCoordinatesTable.setActive(!btnShowCoordinatesTable.isActive());
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

    public void updateSidebarWithNewData() {
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

    public void openProjectPropertiesDialog() {
        ProjectPropertiesDialog dialog = new ProjectPropertiesDialog(projectData);

        Optional<Boolean> result = dialog.showAndWait();

        if (result.isPresent() && result.get()) {
            System.out.println("Dados do projeto atualizados.");

            if (propertiesSidebar != null) {
                propertiesSidebar.refreshData();
            }
        }
    }

    public void loadLocationImage() {
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

    // 1. Substitua o método handleSelectionUpdate por esta versão mais inteligente
    private void handleSelectionUpdate(TopoPoint interactionPoint) {
        // A. Atualiza o Overlay Flutuante (Info visual do mouse)
        if (interactionPoint == null) {
            // Só esconde se não tiver nada selecionado visualmente
            if (cadCanvas.getAllPoints().stream().noneMatch(TopoPoint::isSelected)) {
                infoOverlay.setVisible(false);
            }
        } else {
            updateInfoOverlay(interactionPoint);
            infoOverlay.setVisible(true);
        }

        // B. Coleta os pontos para a Tabela e Resumo
        List<TopoPoint> pointsToShow = new ArrayList<>();

        // Verifica se existe ALGUM ponto selecionado no desenho (ignorando Folhas)
        boolean hasSelection = false;
        TopoObject singleSelectedPoly = null;
        int selectedObjectsCount = 0;

        for (TopoObject obj : cadCanvas.getObjects()) {
            // Ignora camadas de sistema
            if (obj.getLayerName() != null && obj.getLayerName().startsWith("FOLHA")) continue;
            if (obj.getLayerName() != null && obj.getLayerName().equals("GRID")) continue;

            // Verifica quantos pontos deste objeto estão selecionados
            List<TopoPoint> objSelectedPoints = new ArrayList<>();
            for (TopoPoint p : obj.getPoints()) {
                if (p.isSelected()) {
                    objSelectedPoints.add(p);
                    hasSelection = true;
                }
            }

            // Se tiver pontos selecionados, adiciona à lista de exibição
            if (!objSelectedPoints.isEmpty()) {
                // Se o objeto inteiro estiver selecionado (ou quase), preservamos a ordem do objeto
                // Isso garante que o cálculo de Área funcione corretamente
                if (objSelectedPoints.size() == obj.getPoints().size()) {
                    pointsToShow.addAll(obj.getPoints());
                    if (obj.isClosed()) {
                        singleSelectedPoly = obj;
                        selectedObjectsCount++;
                    }
                } else {
                    // Seleção parcial: adiciona apenas os pontos selecionados
                    pointsToShow.addAll(objSelectedPoints);
                }
            }
        }

        // C. Atualiza a Tabela e o Resumo (Área/Perímetro)
        if (hasSelection) {
            // MODO SELEÇÃO: Mostra apenas o que o usuário escolheu

            // Prioridade: Se exatamente 1 polígono fechado estiver selecionado,
            // garantimos que a tabela mostre a ordem correta dele para fechar o cálculo de área.
            if (selectedObjectsCount == 1 && singleSelectedPoly != null) {
                updateCoordinatesTable(singleSelectedPoly.getPoints());
            } else {
                // Seleção mista ou parcial: Mostra a lista acumulada
                // (O cálculo de área pode dar zero se não fechar, o que é correto)
                updateCoordinatesTable(pointsToShow);
            }
        } else {
            // MODO PADRÃO: Nada selecionado -> Mostra TUDO (Exceto folhas)
            updateCoordinatesTable(cadCanvas.getSurveyPoints());
        }
    }

    public void handleShowLayers(){
        ContextMenu layerMenu = new ContextMenu();

        CheckMenuItem itemMestra = new CheckMenuItem("Curvas Mestras (Cotas)");
        itemMestra.setSelected(cadCanvas.isLayerVisible("CURVA_MESTRA"));
        itemMestra.setOnAction(ev -> cadCanvas.setLayerVisible("CURVA_MESTRA", itemMestra.isSelected()));

        CheckMenuItem itemNormal = new CheckMenuItem("Curvas Intermediárias");
        itemNormal.setSelected(cadCanvas.isLayerVisible("CURVA_NORMAL"));
        itemNormal.setOnAction(ev -> cadCanvas.setLayerVisible("CURVA_NORMAL", itemNormal.isSelected()));

        CheckMenuItem itemDefault = new CheckMenuItem("Pontos & Perímetro");
        itemDefault.setSelected(cadCanvas.isLayerVisible("DEFAULT"));
        itemDefault.setOnAction(ev -> cadCanvas.setLayerVisible("DEFAULT", itemDefault.isSelected()));

        CheckMenuItem itemText = new CheckMenuItem("Textos");
        itemText.setSelected(cadCanvas.isLayerVisible("TEXT"));
        itemText.setOnAction(ev -> cadCanvas.setLayerVisible("TEXT", itemText.isSelected()));

        CheckMenuItem itemCompass = new CheckMenuItem("Rosa dos Ventos");
        itemCompass.setSelected(cadCanvas.isLayerVisible("ROSA_VENTOS"));
        itemCompass.setOnAction(ev -> cadCanvas.setLayerVisible("ROSA_VENTOS", itemCompass.isSelected()));

        layerMenu.getItems().addAll(itemMestra, itemNormal, new SeparatorMenuItem(), itemDefault, itemText,new SeparatorMenuItem(),
                itemCompass);

        layerMenu.show(btnLayers, Side.BOTTOM, 0, 0);
    }

    public void handleCreateContourCurves() {
        TextInputDialog dialog = new TextInputDialog("1.0");
        dialog.setTitle("Gerar Curvas de Nível");
        dialog.setHeaderText("Altimetria Automática (TIN)");
        dialog.setContentText("Digite a Equidistância Vertical (m):");

        dialog.showAndWait().ifPresent(intervalStr -> {
            try {
                double interval = Double.parseDouble(intervalStr.replace(",", "."));

                List<TopoPoint> allPoints = cadCanvas.getAllPoints();

                if (allPoints.size() < 3) {
                    showAlert("Dados Insuficientes", "São necessários pelo menos 3 pontos com cota para triangular.");
                    return;
                }

                System.out.println("Iniciando triangulação e curvas com intervalo: " + interval);

                List<TopoObject> curvas = ContourGenerator.generateContours(allPoints, interval);

                if (curvas.isEmpty()) {
                    showAlert("Aviso", "Nenhuma curva gerada. Verifique se os pontos têm variação de cota Z.");
                } else {
                    cadCanvas.getObjects().addAll(curvas);
                    cadCanvas.redraw();

                    System.out.println("Sucesso: " + curvas.size() + " segmentos gerados.");
                }

            } catch (NumberFormatException ex) {
                showAlert("Erro", "Número inválido. Use ponto para decimais (ex: 0.5).");
            }
        });
    }

    private void handleConfigVertices() {
        selectTool(btnConfigVertices, HandleFunctions.FunctionType.CONFIG_VERTICES);

        functions.setOnActionFinished(() -> {
            TopoObject obj = functions.getTempObject();
            int clickedIndex = functions.getTempIndex(); // Este será o novo Ponto 0

            if (obj != null) {
                // --- CRIA O DIÁLOGO CUSTOMIZADO ---
                Dialog<ButtonType> dialog = new Dialog<>();
                dialog.setTitle("Configurar Vértices");
                dialog.setHeaderText("Configuração de Poligonal");

                ButtonType btnApply = new ButtonType("Aplicar", ButtonBar.ButtonData.OK_DONE);
                dialog.getDialogPane().getButtonTypes().addAll(btnApply, ButtonType.CANCEL);

                GridPane grid = new GridPane();
                grid.setHgap(10);
                grid.setVgap(10);
                grid.setPadding(new Insets(20, 150, 10, 10));

                // 1. Opções de Geometria
                CheckBox chkSetStart = new CheckBox("Definir este vértice como Início (Ponto 1)");
                chkSetStart.setSelected(true);

                CheckBox chkReverse = new CheckBox("Inverter Sentido (Horário <-> Anti-horário)");

                // 2. Opções de Renomeação
                CheckBox chkRename = new CheckBox("Renomear Pontos em Massa");
                chkRename.setSelected(true); // Padrão selecionado

                TextField txtPrefix = new TextField("PT");
                txtPrefix.setPromptText("Prefixo");

                TextField txtSeparator = new TextField("-");
                txtSeparator.setPromptText("Separador");

                TextField txtStartNum = new TextField("01");

                // Layout
                grid.add(new Label("Geometria:"), 0, 0);
                grid.add(chkSetStart, 0, 1, 2, 1);
                grid.add(chkReverse, 0, 2, 2, 1);

                grid.add(new Separator(), 0, 3, 2, 1);

                grid.add(new Label("Renomeação:"), 0, 4);
                grid.add(chkRename, 0, 5, 2, 1);

                grid.add(new Label("Prefixo:"), 0, 6);
                grid.add(txtPrefix, 1, 6);

                grid.add(new Label("Separador:"), 0, 7);
                grid.add(txtSeparator, 1, 7);

                grid.add(new Label("Início:"), 0, 8);
                grid.add(txtStartNum, 1, 8);

                // Habilita/Desabilita campos de texto baseado no checkbox
                txtPrefix.disableProperty().bind(chkRename.selectedProperty().not());
                txtSeparator.disableProperty().bind(chkRename.selectedProperty().not());
                txtStartNum.disableProperty().bind(chkRename.selectedProperty().not());

                dialog.getDialogPane().setContent(grid);

                Optional<ButtonType> result = dialog.showAndWait();

                if (result.isPresent() && result.get() == btnApply) {
                    if (chkSetStart.isSelected()) {
                        obj.setStartPointIndex(clickedIndex);
                        System.out.println("Novo ponto inicial definido no índice antigo: " + clickedIndex);
                    }

                    if (chkReverse.isSelected()) {
                        obj.reverseDirection();
                        System.out.println("Sentido invertido.");
                    }

                    // 3. Renomeia
                    if (chkRename.isSelected()) {
                        String prefix = txtPrefix.getText();
                        String sep = txtSeparator.getText();
                        int startN = 1;
                        try {
                            startN = Integer.parseInt(txtStartNum.getText());
                        } catch(Exception ex){}

                        obj.batchRename(prefix, sep, startN);
                        System.out.println("Pontos renomeados.");
                    }

                    // Atualiza Canvas e Tabela
                    cadCanvas.redraw();
                    updateCoordinatesTable(obj.getPoints());
                }
            }

            // Desativa botão
            btnConfigVertices.setActive(false);
            functions.setFunction(HandleFunctions.FunctionType.NONE);
            functions.setOnActionFinished(null);
            updateToolTip("");
        });
    }

    public void handleImportPointsFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Importar Pontos Topográficos");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Arquivos de Texto", "*.txt", "*.csv")
        );

        File file = fileChooser.showOpenDialog(getScene().getWindow());

        if (file != null) {
            try {
                List<TopoPoint> pontosImportados = PointImporter.importFromCSV(file);

                if (pontosImportados.isEmpty()) {
                    showAlert("Aviso", "Nenhum ponto encontrado no arquivo.");
                    return;
                }

                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Opções de Importação");
                alert.setHeaderText("Foram encontrados " + pontosImportados.size() + " pontos.");
                alert.setContentText("Como deseja inserir estes dados no desenho?");

                ButtonType btnPontos = new ButtonType("Apenas Pontos");
                ButtonType btnPerimetro = new ButtonType("Ligar Perímetro");
                ButtonType btnCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);

                alert.getButtonTypes().setAll(btnPontos, btnPerimetro, btnCancelar);

                Optional<ButtonType> result = alert.showAndWait();

                if (result.isPresent()) {
                    if (result.get() == btnPontos) {
                        for(TopoPoint point : pontosImportados) {
                            List<TopoPoint> pointToList = new ArrayList<>();
                            pointToList.add(point);
                            TopoObject nuvem = new TopoObject(pointToList, false);
                            nuvem.setId("IMPORT-" + System.currentTimeMillis());

                            nuvem.setLayerName("NUVEM_PONTOS");

                            cadCanvas.getObjects().add(nuvem);
                        }

                        System.out.println("Importados pontos isolados.");

                    } else if (result.get() == btnPerimetro) {
                        cadCanvas.setImportedPoints(pontosImportados);
                        System.out.println("Importado como perímetro fechado.");
                    } else {
                        return;
                    }

                    updateCoordinatesTable(cadCanvas.getSurveyPoints());
                    cadCanvas.zoomExtents();
                    cadCanvas.redraw();
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                showAlert("Erro na Importação", ex.getMessage());
            }
        }
    }

    private void handleExportMemorial() {
        TopoObject targetPoly = findTargetPolygon(); // Método auxiliar abaixo
        if (targetPoly == null) return;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Salvar Memorial Descritivo");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Documento PDF (*.pdf)", "*.pdf"),
                new FileChooser.ExtensionFilter("Documento Word (*.doc)", "*.doc")
        );
        fileChooser.setInitialFileName("Memorial_" + (projectData.getPropertyInfo().getNomePropriedade() != null ? projectData.getPropertyInfo().getNomePropriedade() : "Gleba"));

        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            String path = file.getAbsolutePath().toLowerCase();
            try {
                if (path.endsWith(".pdf")) {
                    com.brasens.model.report.PdfMemorialGenerator.generateAndSave(targetPoly, projectData, file);
                } else {
                    // Fallback para texto/doc
                    com.brasens.model.report.MemorialGenerator.generateAndSave(targetPoly, projectData, file);
                }
                showAlert("Sucesso", "Memorial exportado com sucesso!");
            } catch (Exception e) {
                showAlert("Erro", "Falha ao exportar: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // --- EXPORTAÇÃO MAPA (DXF) ---
    private void handleExportMap() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exportar Mapa de Retificação");

        // Define Filtros para as duas opções solicitadas
        FileChooser.ExtensionFilter filterDxf = new FileChooser.ExtensionFilter("Arquivo DXF (CAD) (*.dxf)", "*.dxf");
        FileChooser.ExtensionFilter filterPdf = new FileChooser.ExtensionFilter("Desenho Técnico PDF (*.pdf)", "*.pdf");

        fileChooser.getExtensionFilters().addAll(filterDxf, filterPdf);

        // Nome padrão
        String defaultName = (projectData.getPropertyInfo().getNomePropriedade() != null && !projectData.getPropertyInfo().getNomePropriedade().isEmpty())
                ? projectData.getPropertyInfo().getNomePropriedade()
                : "Projeto";
        fileChooser.setInitialFileName("Mapa_" + defaultName);

        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            String path = file.getAbsolutePath().toLowerCase();
            try {
                // OPÇÃO 1: PDF (Desenho Técnico 2D com Barra Lateral)
                if (path.endsWith(".pdf")) {
                    // Pega todos os objetos visíveis
                    List<TopoObject> mapObjects = cadCanvas.getObjects();

                    com.brasens.model.io.PdfMapPlotter.export(mapObjects, projectData, file);

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Sucesso");
                    alert.setHeaderText(null);
                    alert.setContentText("Mapa PDF gerado com sucesso!\nVerifique a barra lateral e tabelas no arquivo.");
                    alert.showAndWait();
                }
                // OPÇÃO 2: DXF (Apenas geometria para outros softwares)
                else {
                    // Garante extensão dxf se o usuário esqueceu
                    if (!path.endsWith(".dxf")) {
                        file = new File(file.getAbsolutePath() + ".dxf");
                    }

                    // Usa a classe DxfExport existente (ou SimpleDxfParser se for o caso, mas o DxfExport é para escrita)
                    // Assumindo que você criou o DxfExport na etapa anterior:
                    com.brasens.model.io.DxfExport.export(cadCanvas.getObjects(), file);

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Sucesso");
                    alert.setHeaderText(null);
                    alert.setContentText("Arquivo DXF exportado com sucesso!");
                    alert.showAndWait();
                }
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Erro", "Falha ao exportar mapa: " + e.getMessage());
            }
        }
    }

    // Auxiliar para achar qual polígono o usuário quer exportar
    private TopoObject findTargetPolygon() {
        // 1. Tenta o selecionado
        for(TopoObject obj : cadCanvas.getObjects()) {
            if (obj.isClosed() && !obj.getPoints().isEmpty() && obj.getPoints().get(0).isSelected()
                    && !obj.getLayerName().startsWith("FOLHA")) {
                return obj;
            }
        }
        // 2. Se houver apenas 1 polígono fechado de levantamento, usa ele
        TopoObject candidate = null;
        int count = 0;
        for(TopoObject obj : cadCanvas.getObjects()) {
            if (obj.isClosed() && !obj.getLayerName().startsWith("FOLHA") && !obj.getLayerName().equals("GRID")) {
                candidate = obj;
                count++;
            }
        }
        if (count == 1) return candidate;

        showAlert("Atenção", "Selecione o polígono (gleba) que deseja gerar o memorial.");
        return null;
    }

    public void handleJoinObjects() {
        // 1. Identifica objetos selecionados
        List<TopoObject> selectedObjects = new ArrayList<>();

        for (TopoObject obj : cadCanvas.getObjects()) {
            if (!obj.getPoints().isEmpty() && obj.getPoints().get(0).isSelected()) {
                selectedObjects.add(obj);
            }
        }

        if (selectedObjects.size() < 2) {
            showAlert("Aviso", "Selecione pelo menos 2 linhas conectadas para unir.");
            return;
        }

        // 2. Executa a união
        List<TopoObject> joinedResult = com.brasens.utilities.math.GeometryUtils.joinObjects(selectedObjects);

        // 3. Atualiza o Canvas
        // Remove os pedaços velhos
        List<TopoObject> canvasList = new ArrayList<>(cadCanvas.getObjects()); // Cópia segura
        canvasList.removeAll(selectedObjects);

        // Adiciona os novos unidos
        canvasList.addAll(joinedResult);

        cadCanvas.setObjects(canvasList);
        cadCanvas.clearSelection();
        cadCanvas.redraw();

        // 4. Feedback
        int reduced = selectedObjects.size() - joinedResult.size();
        System.out.println("União concluída. " + selectedObjects.size() + " objetos viraram " + joinedResult.size() + ".");

        // Se resultou em um polígono fechado único, atualiza a barra lateral
        if (joinedResult.size() == 1 && joinedResult.get(0).isClosed()) {
            updateCoordinatesTable(joinedResult.get(0).getPoints());
            System.out.println("Objeto fechado detectado! Área calculada.");
        }
    }

    public void handleDivideArea() {
        // 1. Verifica se tem UM polígono selecionado
        TopoObject tempPoly = null;
        int count = 0;

        for(TopoObject obj : cadCanvas.getObjects()) {
            boolean isSel = false;
            if (!obj.getPoints().isEmpty() && obj.getPoints().get(0).isSelected()) {
                isSel = true;
            }

            if (isSel && obj.isClosed()) {
                tempPoly = obj;
                count++;
            }
        }

        if (count != 1 || tempPoly == null) {
            showAlert("Seleção Inválida", "Selecione exatamente UM polígono fechado antes de clicar na ferramenta.");
            return;
        }

        final TopoObject selectedPoly = tempPoly;

        // 2. Diálogo
        Dialog<javafx.util.Pair<Double, Double>> dialog = new Dialog<>();
        dialog.setTitle("Divisão de Área");
        dialog.setHeaderText("Configurar Divisão da Gleba");

        ButtonType loginButtonType = new ButtonType("Dividir", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField areaField = new TextField();
        areaField.setPromptText("Ex: 2.5000");
        TextField azimuthField = new TextField();
        azimuthField.setPromptText("Ex: 90.0");

        grid.add(new Label("Área Desejada (ha):"), 0, 0);
        grid.add(areaField, 1, 0);
        grid.add(new Label("Azimute da Linha de Corte (Graus):"), 0, 1);
        grid.add(azimuthField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                try {
                    double area = Double.parseDouble(areaField.getText().replace(",", "."));
                    double az = Double.parseDouble(azimuthField.getText().replace(",", "."));
                    return new javafx.util.Pair<>(area, az);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        });

        Optional<javafx.util.Pair<Double, Double>> result = dialog.showAndWait();

        result.ifPresent(pair -> {
            double targetArea = pair.getKey();
            double azimuth = pair.getValue();

            System.out.println("Dividindo área de " + selectedPoly.getAreaHa() + "ha. Alvo: " + targetArea + "ha. Az: " + azimuth);

            // 3. Chama o Motor Matemático
            List<TopoObject> newPolys = com.brasens.utilities.math.AreaDivider.dividePolygon(selectedPoly, targetArea, azimuth);

            if (!newPolys.isEmpty()) {
                // Cria cópia da lista atual
                List<TopoObject> newList = new ArrayList<>(cadCanvas.getObjects());
                newList.remove(selectedPoly);

                // Adiciona os novos polígonos validando se são PERIMETRO
                for (TopoObject poly : newPolys) {
                    poly.validatePerimeter(); // <--- APLICA A REGRA AQUI
                    newList.add(poly);
                }

                cadCanvas.setObjects(newList);

                cadCanvas.clearSelection();
                cadCanvas.redraw();

                // Seleciona o primeiro pedaço para atualizar a UI
                if (newPolys.get(0).getPoints().size() > 0) {
                    updateCoordinatesTable(newPolys.get(0).getPoints());
                }

                System.out.println("Divisão concluída com sucesso!");
            } else {
                showAlert("Erro na Divisão", "Não foi possível dividir a área com estes parâmetros.\nVerifique se a área alvo é menor que a total.");
            }
        });

        btnDivideArea.setActive(false);
    }

    public void handleAddTable() {

        if (btnTable.isActive()) {
            btnTable.setActive(false);
            functions.setFunction(HandleFunctions.FunctionType.NONE);
            if (cadCanvas.getScene() != null) cadCanvas.getScene().setCursor(Cursor.DEFAULT);
            functions.setOnActionFinished(null);
            return;
        }

        List<TopoPoint> pointsForTable = new ArrayList<>();
        boolean isSelectionMode = false;

        for (TopoObject obj : cadCanvas.getObjects()) {
            if (obj.getLayerName() != null && obj.getLayerName().startsWith("FOLHA")) continue;
            if ("GRID".equals(obj.getLayerName())) continue;

            boolean isObjSelected = false;
            for (TopoPoint p : obj.getPoints()) {
                if (p.isSelected()) {
                    isObjSelected = true;
                    break;
                }
            }

            if (isObjSelected) {
                isSelectionMode = true;
                pointsForTable.addAll(obj.getPoints());
            }
        }

        if (!isSelectionMode) {
            pointsForTable = cadCanvas.getSurveyPoints();
        }

        if (pointsForTable.size() > 1 && pointsForTable.get(0) == pointsForTable.get(pointsForTable.size() - 1)) {

        }

        if (pointsForTable.isEmpty()) {
            showAlert("Aviso", "Não há pontos disponíveis para gerar a tabela.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Inserir Tabela");

        String modeText = isSelectionMode ? " (Baseado na Seleção)" : " (Todos os Pontos)";
        alert.setHeaderText("Configurar Tabela" + modeText);
        alert.setContentText("Serão listados " + pointsForTable.size() + " pontos.\nEscolha o modelo:");

        ButtonType btnSimples = new ButtonType("Coordenadas (XYZ)");
        ButtonType btnMemorial = new ButtonType("Memorial Descritivo");
        ButtonType btnCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(btnSimples, btnMemorial, btnCancelar);

        alert.getDialogPane().lookupButton(btnMemorial).setDisable(pointsForTable.size() < 2);

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isEmpty() || result.get() == btnCancelar) return;

        boolean isMemorial = (result.get() == btnMemorial);

        final List<TopoPoint> finalPoints = pointsForTable;

        selectTool(btnTable, HandleFunctions.FunctionType.PLACE_TABLE);
        if (cadCanvas.getScene() != null) {
            cadCanvas.getScene().setCursor(Cursor.CROSSHAIR);
        }

        functions.setOnActionFinished(() -> {
            int lastIdx = cadCanvas.getObjects().size() - 1;

            if (lastIdx >= 0 && cadCanvas.getObjects().get(lastIdx) instanceof com.brasens.model.objects.TopoTableObject) {
                com.brasens.model.objects.TopoTableObject tableObj =
                        (com.brasens.model.objects.TopoTableObject) cadCanvas.getObjects().get(lastIdx);

                if (isMemorial) {
                    configureMemorialTable(tableObj, finalPoints);
                } else {
                    configureSimpleTable(tableObj, finalPoints);
                }

                cadCanvas.redraw();
            }

            btnTable.setActive(false);
            System.out.println("Tabela inserida com " + finalPoints.size() + " pontos.");
            updateToolTip("");
        });
    }

    private void configureSimpleTable(com.brasens.model.objects.TopoTableObject table, List<TopoPoint> points) {
        table.updateDataFromPoints(points);
    }

    private void configureMemorialTable(com.brasens.model.objects.TopoTableObject table, List<TopoPoint> points) {
        String[] headers = {"VÉRTICE", "AZIMUTE", "DISTÂNCIA", "COORD. ESTE (X)", "COORD. NORTE (Y)", "CONFRONTANTE"};
        List<String[]> rows = new ArrayList<>();

        int n = points.size();

        boolean isClosedLoop = (n > 2 && isLoop(points));
        int iterations = isClosedLoop ? n : n - 1;

        for (int i = 0; i < iterations; i++) {
            TopoPoint current = points.get(i);
            TopoPoint next = points.get((i + 1) % n);

            // Cálculos usando a biblioteca matemática
            double dist = TopologyMath.getDistance2D(current, next);
            double az = TopologyMath.getAzimuth(current, next);
            String azStr = TopologyMath.degreesToDMS(az);

            String[] row = new String[6];
            row[0] = current.getName();                                    // Vértice
            row[1] = azStr;                                                // Azimute
            row[2] = String.format("%.2f", dist);                          // Distância
            row[3] = String.format("%.3f", current.getX());                // X
            row[4] = String.format("%.3f", current.getY());                // Y

            row[5] = " - ";

            rows.add(row);
        }

        if (!isClosedLoop && n > 0) {
            TopoPoint last = points.get(n - 1);
            String[] row = new String[] {
                    last.getName(), "-", "-",
                    String.format("%.3f", last.getX()),
                    String.format("%.3f", last.getY()),
                    "-"
            };
            rows.add(row);
        }

        table.setCustomData(headers, rows);
        table.setColWidth(25.0);
    }

    private boolean isLoop(List<TopoPoint> pts) {
        if(pts.size() < 3) return false;
        TopoPoint first = pts.get(0);
        TopoPoint last = pts.get(pts.size()-1);
        return Math.hypot(first.getX()-last.getX(), first.getY()-last.getY()) < 0.01;
    }

    private void configureMemorialTable(com.brasens.model.objects.TopoTableObject table) {
        String[] headers = {"VÉRTICE", "AZIMUTE", "DISTÂNCIA", "COORD. ESTE (X)", "COORD. NORTE (Y)", "CONFRONTANTE"};
        List<String[]> rows = new ArrayList<>();

        ObservableList<CoordinateRow> data = coordinateTableView.getItems();

        for (CoordinateRow item : data) {
            String[] row = new String[6];
            row[0] = item.deProperty().get();          // Vértice (De)
            row[1] = item.azimuteProperty().get();     // Azimute
            row[2] = item.distanciaProperty().get();   // Distância
            row[3] = item.coordEProperty().get();      // X
            row[4] = item.coordNProperty().get();      // Y
            row[5] = " - ";                            // Confrontante (Placeholder, editável depois)

            rows.add(row);
        }

        table.setCustomData(headers, rows);
        table.setColWidth(25.0); // Colunas um pouco mais largas para caber Azimute
    }

    public void handleShowGrid(){
        boolean newState = !cadCanvas.isShowBackgroundGrid();
        cadCanvas.setShowBackgroundGrid(newState);
        cadCanvas.redraw();
        btnToggleGrid.setActive(newState);
    }

    private void handleInsertSheet() {
        // Diálogo de Configuração
        Dialog<Pair<SheetManager.SheetFormat, Double>> dialog = new Dialog<>();
        dialog.setTitle("Inserir Folha");
        dialog.setHeaderText("Configuração de Prancha");

        ButtonType okButtonType = new ButtonType("Inserir", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ComboBox<com.brasens.utilities.common.SheetManager.SheetFormat> cboFormat = new ComboBox<>();
        cboFormat.getItems().addAll(com.brasens.utilities.common.SheetManager.SheetFormat.values());
        cboFormat.getSelectionModel().select(com.brasens.utilities.common.SheetManager.SheetFormat.A1); // Padrão

        TextField txtScale = new TextField("1000"); // Padrão 1:1000

        grid.add(new Label("Formato:"), 0, 0);
        grid.add(cboFormat, 1, 0);
        grid.add(new Label("Escala (1:):"), 0, 1);
        grid.add(txtScale, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                try {
                    double s = Double.parseDouble(txtScale.getText());
                    return new Pair<>(cboFormat.getValue(), s);
                } catch (Exception e) {
                    return null;
                }
            }
            return null;
        });

        Optional<Pair<com.brasens.utilities.common.SheetManager.SheetFormat, Double>> result = dialog.showAndWait();

        result.ifPresent(pair -> {
            // Configura a função para o Canvas desenhar o Ghost
            functions.setTempSheetFormat(pair.getKey());
            functions.setTempSheetScale(pair.getValue());

            selectTool(btnInsertSheet, HandleFunctions.FunctionType.PLACE_SHEET);

            // Callback de desligamento
            functions.setOnActionFinished(() -> {
                btnInsertSheet.setActive(false);
                updateToolTip("");
            });

            System.out.println("Posicione a folha " + pair.getKey() + " na tela.");
        });
    }

    private void createTipsOverlay() {
        lblToolTip = new Label("");
        lblToolTip.setTextFill(Color.WHITE);
        lblToolTip.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        lblToolTip.setPadding(new Insets(8, 15, 8, 15));

        lblToolTip.setStyle(
                "-fx-background-color: rgba(0, 0, 0, 0.7);" +
                        "-fx-background-radius: 20;" +
                        "-fx-border-radius: 20;" +
                        "-fx-border-color: rgba(255, 255, 255, 0.3);" +
                        "-fx-border-width: 1;"
        );

        tipsContainer = new StackPane(lblToolTip);
        tipsContainer.setMouseTransparent(true); // Deixa o clique passar para o canvas
        tipsContainer.setPadding(new Insets(0, 0, 20, 0)); // Margem inferior

        // Inicialmente invisível
        tipsContainer.setVisible(false);
    }

    private void updateToolTip(String text) {
        if (text == null || text.isEmpty()) {
            tipsContainer.setVisible(false);
        } else {
            lblToolTip.setText(text);
            tipsContainer.setVisible(true);
            tipsContainer.toFront();
        }
    }

    private VBox createSection(String title, javafx.scene.Node... nodes) {
        HBox buttonsBox = new HBox(5);
        buttonsBox.setAlignment(javafx.geometry.Pos.CENTER);
        buttonsBox.getChildren().addAll(nodes);

        Label lblTitle = new Label(title);
        lblTitle.setTextFill(Color.web("#888888"));
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 9));

        lblTitle.setPadding(new Insets(2, 0, 4, 0));

        VBox sectionBox = new VBox();
        sectionBox.setAlignment(javafx.geometry.Pos.CENTER);
        sectionBox.getChildren().addAll(buttonsBox, lblTitle);

        return sectionBox;
    }

    private ToolBar createMainToolBar() {
        ToolBar toolBar = new ToolBar();

        double height = 70.0;
        toolBar.setPrefHeight(height);
        toolBar.setMinHeight(height);
        toolBar.setMaxHeight(height);

        toolBar.setPadding(new javafx.geometry.Insets(5, 10, 5, 10));

        Color colorDefault = Color.TRANSPARENT;
        Color colorHover = Color.web("#3f3f3f"); // Um cinza levemente mais claro para hover
        Color colorActive = Color.web("#0078D7"); // Azul para ativo

        int btnImageSize = 18;

        // --- CAD GERAL ---
        btnImport = new CustomButton("Importar", new Image(CAD.class.getResource("/mspm/icons/import.png").toString()), "", btnImageSize);
        btnSave = new CustomButton("Salvar", new Image(CAD.class.getResource("/mspm/icons/save.png").toString()), "", btnImageSize);
        btnPan = new CustomButton("Pan", new Image(CAD.class.getResource("/mspm/icons/move.png").toString()), "", btnImageSize);
        btnZoomExtents = new CustomButton("Zoom Ext.", new Image(CAD.class.getResource("/mspm/icons/zoom-in.png").toString()), "", btnImageSize);
        btnToggleGrid = new CustomButton("Grade", new Image(CAD.class.getResource("/mspm/icons/pixels.png").toString()), "", btnImageSize);
        btnLayers = new CustomButton("Camadas", new Image(CAD.class.getResource("/mspm/icons/layers.png").toString()), "", btnImageSize);
        btnLine = new CustomButton("Linha", new Image(CAD.class.getResource("/mspm/icons/nodes.png").toString()), "", btnImageSize);
        btnPolyline = new CustomButton("Polilinha", new Image(CAD.class.getResource("/mspm/icons/polyline.png").toString()), "", btnImageSize);
        btnJoin = new CustomButton("Unir", new Image(CAD.class.getResource("/mspm/icons/broken-link.png").toString()), "", btnImageSize);
        btnUIText = new CustomButton("Texto", new Image(CAD.class.getResource("/mspm/icons/text.png").toString()), "", btnImageSize);
        btnShowCoordinatesTable = new CustomButton("Painel Lat.", new Image(CAD.class.getResource("/mspm/icons/table-grid.png").toString()), "", btnImageSize);
        btnDimArea = new CustomButton("Área", new Image(CAD.class.getResource("/mspm/icons/area.png").toString()), "", btnImageSize);
        btnDimSegments = new CustomButton("Distância", new Image(CAD.class.getResource("/mspm/icons/measure.png").toString()), "", btnImageSize);
        btnDimAngle = new CustomButton("Ângulo", new Image(CAD.class.getResource("/mspm/icons/angle.png").toString()), "", btnImageSize);

        btnImport.setAnimation(colorDefault, colorHover, colorActive, 200, false);
        btnImport.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                handleImportPointsFile();
            }
        });

        btnImport.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                handleImportPointsFile();
            }
        });

        btnSave.setAnimation(colorDefault, colorHover, colorActive, 200, false);
        btnSave.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                saveProject();
            }
        });

        btnZoomExtents.setAnimation(colorDefault, colorHover, colorActive, 200, false);
        btnZoomExtents.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                getCadCanvas().zoomExtents();
            }
        });

        btnPan.setAnimation(colorDefault, colorHover, colorActive, 200, true);
        btnPan.setOnMouseClicked(e -> {
            selectTool(btnPan, HandleFunctions.FunctionType.EDGEPAN);
            functions.setEdgePanEnabled(!functions.isEdgePanEnabled());
        });

        btnLine.setAnimation(colorDefault, colorHover, colorActive, 200, true);
        btnLine.setOnMouseClicked(e -> {
            selectTool(btnLine, HandleFunctions.FunctionType.LINE);
        });

        btnPolyline.setAnimation(colorDefault, colorHover, colorActive, 200, true);
        btnPolyline.setOnMouseClicked(e -> {
            selectTool(btnPolyline, HandleFunctions.FunctionType.POLYLINE);
        });

        btnJoin.setAnimation(colorDefault, colorHover, colorActive, 200, false); // False = Click único, não toggle
        btnJoin.setOnMouseClicked(e -> {
            handleJoinObjects();
        });

        btnUIText.setAnimation(colorDefault, colorHover, colorActive, 200, true);
        btnUIText.setOnMouseClicked(e -> {
            selectTool(btnUIText, HandleFunctions.FunctionType.TEXT);
        });


        btnShowCoordinatesTable.setAnimation(colorDefault, colorHover, colorActive, 200, true);
        btnShowCoordinatesTable.setOnMouseClicked(e -> {
            toggleTable();
            btnShowCoordinatesTable.setActive(!btnShowCoordinatesTable.isActive());
        });
        btnShowCoordinatesTable.setActive(true);

        btnLayers.setAnimation(colorDefault, colorHover, colorActive, 200, true);

        btnLayers.setOnMouseClicked(e -> {
            handleShowLayers();
        });

        btnToggleGrid.setAnimation(colorDefault, colorHover, colorActive, 200, true);
        btnToggleGrid.setActive(true);

        btnToggleGrid.setOnMouseClicked(e -> {
            handleShowGrid();
        });

        btnDimArea.setAnimation(colorDefault, colorHover, colorActive, 200, true);
        btnDimArea.setOnMouseClicked(e -> {
            selectTool(btnDimArea, HandleFunctions.FunctionType.DIMENSION_AREA);

            // Configura o reset automático após o uso
            functions.setOnActionFinished(() -> {
                btnDimArea.setActive(false);
                functions.setFunction(HandleFunctions.FunctionType.NONE);
                System.out.println("Ferramenta Área finalizada.");
                updateToolTip("");
            });

            System.out.println("Ferramenta Cotar Área Selecionada. Clique na borda de um polígono.");
        });

        btnDimSegments.setAnimation(colorDefault, colorHover, colorActive, 200, true);
        btnDimSegments.setOnMouseClicked(e -> {
            selectTool(btnDimSegments, HandleFunctions.FunctionType.DIMENSION_SEGMENTS);

            // Configura o reset automático após o uso
            functions.setOnActionFinished(() -> {
                btnDimSegments.setActive(false);
                functions.setFunction(HandleFunctions.FunctionType.NONE);
                System.out.println("Ferramenta Segmentos finalizada.");
                updateToolTip("");
            });

            System.out.println("Ferramenta Cotar Segmentos Selecionada. Clique em uma linha/polígono.");
        });

        btnDimAngle.setAnimation(colorDefault, colorHover, colorActive, 200, true);
        btnDimAngle.setOnMouseClicked(e -> {
            selectTool(btnDimAngle, HandleFunctions.FunctionType.DIMENSION_ANGLE);

            // Configura o reset automático após o uso
            functions.setOnActionFinished(() -> {
                btnDimAngle.setActive(false);
                functions.setFunction(HandleFunctions.FunctionType.NONE);
                System.out.println("Ferramenta Ângulo finalizada.");
                updateToolTip("");
            });

            // Reseta variáveis de estado
            functions.setAngleStep(0);
            functions.setAngleVertex(null);
            functions.setAngleP1(null);
            System.out.println("Ferramenta Ângulo: Clique num vértice (Auto) ou no Vazio (Manual).");
        });

        btnDimAngle.setAnimation(colorDefault, colorHover, colorActive, 200, true);

        toolBar.getItems().addAll(
                createSection("ARQUIVO", btnImport, btnSave),
                new Separator(Orientation.VERTICAL),

                createSection("EXIBIR", btnPan, btnZoomExtents, btnToggleGrid, btnLayers, btnShowCoordinatesTable),
                new Separator(Orientation.VERTICAL),

                createSection("DESENHO", btnLine, btnPolyline, btnJoin, btnUIText),
                new Separator(Orientation.VERTICAL),

                createSection("MEDIÇÃO", btnDimArea, btnDimSegments, btnDimAngle)
        );

        return toolBar;
    }

    private ToolBar createMap10XToolBar() {
        ToolBar toolBar = new ToolBar();

        double height = 70.0; // Altura maior para ícone + texto
        toolBar.setPrefHeight(height);
        toolBar.setMinHeight(height);
        toolBar.setMaxHeight(height);

        int btnImageSize = 18;

        toolBar.setPadding(new javafx.geometry.Insets(5, 10, 5, 10));

        toolBar.setStyle("-fx-base: #2b2b2b; -fx-background-color: #2b2b2b;");

        Label lblMode = new Label("MAPA 10X");
        lblMode.setTextFill(Color.web("#888888"));
        lblMode.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        lblMode.setRotate(-90);

        btnTable = new CustomButton("Tabela", new Image(CAD.class.getResource("/mspm/icons/data.png").toString()), "", btnImageSize);
        btnContour = new CustomButton("Gerar Curvas", new Image(CAD.class.getResource("/mspm/icons/contour.png").toString()), "", btnImageSize);
        btnDivideArea = new CustomButton("Div. Área", new Image(CAD.class.getResource("/mspm/icons/divided.png").toString()), "", btnImageSize);
        btnConfrontante = new CustomButton("Confront.", new Image(CAD.class.getResource("/mspm/icons/neighborhood.png").toString()), "", btnImageSize);
        btnInsertSheet = new CustomButton("Folha", new Image(CAD.class.getResource("/mspm/icons/document.png").toString()), "", btnImageSize);
        btnConfigVertices = new CustomButton("Ren. Vértices", new Image(CAD.class.getResource("/mspm/icons/refresh.png").toString()), "", btnImageSize);
        btnMemorial = new CustomButton("Memorial", new Image(CAD.class.getResource("/mspm/icons/google-docs.png").toString()), "", btnImageSize);
        btnExportMap = new CustomButton("Exportar", new Image(CAD.class.getResource("/mspm/icons/share.png").toString()), "", btnImageSize);

        btnTable.setAnimation(colorDefault, colorHover, colorActive, 200, false);
        btnTable.setOnMouseClicked(e -> {
            handleAddTable();
        });

        btnContour.setAnimation(colorDefault, colorHover, colorActive, 200, false);
        btnContour.setOnMouseClicked(e -> {
            handleCreateContourCurves();
        });

        btnDivideArea.setAnimation(colorDefault, colorHover, colorActive, 200, false);
        btnDivideArea.setOnMouseClicked(e -> {
            handleDivideArea();
        });

        btnConfrontante.setAnimation(colorDefault, colorHover, colorActive, 200, true);
        btnConfrontante.setOnMouseClicked(e -> {
            selectTool(btnConfrontante, HandleFunctions.FunctionType.DEFINE_CONFRONTANTE);
            functions.setTempStartPoint(null);
            functions.setOnActionFinished(() -> {
                TopoObject obj = functions.getTempObject();
                int idx = functions.getTempIndex();

                if (obj != null) {
                    TextInputDialog dialog = new TextInputDialog(obj.getConfrontante(idx));
                    dialog.setTitle("Confrontante");
                    dialog.setHeaderText("Definir vizinho para este segmento");
                    dialog.setContentText("Nome do Confrontante:");

                    java.util.Optional<String> result = dialog.showAndWait();
                    result.ifPresent(nome -> {
                        obj.setConfrontante(idx, nome);
                        System.out.println("Confrontante '" + nome + "' definido para o segmento " + idx);
                        cadCanvas.redraw();
                    });
                }

                btnConfrontante.setActive(false);
                functions.setFunction(HandleFunctions.FunctionType.NONE);
                functions.setOnActionFinished(null);

                System.out.println("Ferramenta Confrontante finalizada.");
                updateToolTip("");
            });

            System.out.println("Ferramenta Confrontante: Clique no Ponto A e depois no Ponto B.");
        });

        btnInsertSheet.setAnimation(colorDefault, colorHover, colorActive, 200, false);
        btnInsertSheet.setOnMouseClicked(e -> handleInsertSheet());

        btnConfigVertices.setAnimation(colorDefault, colorHover, colorActive, 200, false);
        btnConfigVertices.setOnMouseClicked(e -> handleConfigVertices());

        btnMemorial.setAnimation(colorDefault, colorHover, colorActive, 200, false);
        btnMemorial.setOnMouseClicked(e -> handleExportMemorial());

        btnExportMap.setAnimation(colorDefault, colorHover, colorActive, 200, false);
        btnExportMap.setOnMouseClicked(e -> handleExportMap());

        toolBar.getItems().addAll(
                lblMode,
                new Separator(Orientation.VERTICAL),

                createSection("LEVANTAMENTO", btnConfigVertices, btnConfrontante, btnTable),

                new Separator(Orientation.VERTICAL),

                createSection("CÁLCULOS", btnDivideArea, btnContour),

                new Separator(Orientation.VERTICAL),

                createSection("PROJETO", btnInsertSheet, btnMemorial, btnExportMap)
        );

        return toolBar;
    }
}