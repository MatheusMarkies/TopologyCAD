package com.brasens.layout.components;

import com.brasens.model.report.ProfessionalSignature;
import com.brasens.model.report.ProjectData;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import lombok.Getter;

import java.util.ArrayList;

@Getter
public class PropertiesSidebar extends ScrollPane {

    private final ProjectData projectData;

    // Labels dinâmicos que precisamos atualizar frequentemente
    private Label lblAreaValue;
    private Label lblPerimeterValue;

    // Container interno
    private VBox contentBox;

    public PropertiesSidebar(ProjectData projectData) {
        this.projectData = projectData;
        initializeUI();
    }

    private void initializeUI() {
        // Configuração do ScrollPane (O container principal)
        this.setFitToWidth(true); // Garante que o conteúdo estique horizontalmente
        this.setHbarPolicy(ScrollBarPolicy.NEVER); // Sem barra horizontal
        this.setStyle("-fx-background: #1e1e1e; -fx-background-color: #1e1e1e; -fx-border-color: #3f3f3f; -fx-border-width: 0 0 0 1;");
        this.setPrefWidth(300);

        // Container Vertical Interno
        contentBox = new VBox();
        contentBox.setPadding(new Insets(20));
        contentBox.setSpacing(20); // Espaço entre as seções (Cards)
        contentBox.setStyle("-fx-background-color: #1e1e1e;");

        // Título Geral
        Label mainTitle = new Label("FICHA TÉCNICA");
        mainTitle.setTextFill(Color.web("#555555"));
        mainTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));

        // --- MONTAGEM DAS SEÇÕES ---

        // 1. Métricas (Área/Perímetro) - Destaque
        VBox metricsCard = createMetricsSection();
        VBox imageSection = createImageSection();
        // 2. Dados do Imóvel
        VBox propertySection = createInfoSection("DADOS DO IMÓVEL",
                new String[][] {
                        {"Propriedade", projectData.getPropertyInfo().getNomePropriedade()},
                        {"Proprietário", projectData.getPropertyInfo().getProprietario()},
                        {"Município", projectData.getPropertyInfo().getMunicipio()},
                        {"Comarca", projectData.getPropertyInfo().getComarca()},
                        {"Matrícula", projectData.getPropertyInfo().getMatricula()},
                        {"Cód. INCRA", projectData.getPropertyInfo().getCodigoIncra()}
                }
        );

        // 3. Especificações Técnicas
        VBox techSection = createInfoSection("ESPECIFICAÇÕES",
                new String[][] {
                        {"Datum", projectData.getTechnicalSpecs().getDatum()},
                        {"Sist. Coord.", projectData.getTechnicalSpecs().getSistemaCoordenadas()},
                        {"Meridiano", projectData.getTechnicalSpecs().getMeridianoCentral()},
                        {"Equipamento", projectData.getTechnicalSpecs().getEquipamento()},
                        {"Escala", projectData.getTechnicalSpecs().getEscalaTexto()}
                }
        );

        // 4. Responsável Técnico (Com proteção contra null)
        VBox respSection = createProfessionalSection();

        // Adiciona tudo ao VBox
        contentBox.getChildren().addAll(mainTitle, metricsCard, imageSection, propertySection, techSection, respSection);

        // Define o conteúdo do ScrollPane
        this.setContent(contentBox);
    }

    // --- CRIAÇÃO DOS BLOCOS ---

    private VBox createImageSection() {
        VBox section = new VBox(10);

        Label lblTitle = new Label("PLANTA DE LOCALIZAÇÃO");
        lblTitle.setTextFill(Color.web("#0078D7"));
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));

        // Container visual para a imagem (Borda e Sombra)
        VBox imageContainer = new VBox();
        imageContainer.setStyle("-fx-background-color: #000; -fx-border-color: #3f3f3f; -fx-border-width: 1;");
        imageContainer.setPadding(new Insets(2)); // Borda interna preta
        imageContainer.setAlignment(Pos.CENTER);

        ImageView imageView = new ImageView();
        Image img = projectData.getPlantaLocalizacao();

        if (img != null) {
            imageView.setImage(img);
        } else {
            // Se não tiver imagem, podemos não mostrar nada ou mostrar um texto "Sem Imagem"
            // Se quiser carregar uma imagem padrão:
            // imageView.setImage(new Image(getClass().getResource("/mspm/icons/no-image.png").toString()));
        }

        // --- AJUSTE DE TAMANHO IMPORTANTE ---
        // O Sidebar tem 300px de largura. Com padding de 20px de cada lado, sobra 260px.
        imageView.setFitWidth(255);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true); // Garante qualidade se redimensionar

        imageContainer.getChildren().add(imageView);

        // Só adiciona o container se tiver imagem (opcional) ou deixa o espaço reservado
        if (img != null) {
            section.getChildren().addAll(lblTitle, imageContainer, new Separator());
        } else {
            // Se for nulo, retorna VBox vazio para não ocupar espaço
            // return new VBox();

            // OU mostra o título com um aviso "Não definida"
            Label lblEmpty = new Label("(Nenhuma imagem definida)");
            lblEmpty.setTextFill(Color.GRAY);
            lblEmpty.setFont(Font.font("Segoe UI", FontWeight.LIGHT, 10));
            section.getChildren().addAll(lblTitle, lblEmpty, new Separator());
        }

        return section;
    }

    private VBox createMetricsSection() {
        VBox box = new VBox(5);
        box.setPadding(new Insets(15));
        // Estilo de "Card" levemente mais claro para destaque
        box.setStyle("-fx-background-color: #2b2b2b; -fx-background-radius: 8; -fx-border-color: #3f3f3f; -fx-border-radius: 8;");

        lblAreaValue = new Label("0.0000 ha");
        lblAreaValue.setTextFill(Color.web("#0078D7")); // Azul
        lblAreaValue.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));

        lblPerimeterValue = new Label("0.00 m");
        lblPerimeterValue.setTextFill(Color.WHITE);
        lblPerimeterValue.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));

        Label titleArea = new Label("ÁREA TOTAL");
        titleArea.setTextFill(Color.GRAY);
        titleArea.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));

        Label titlePerim = new Label("PERÍMETRO");
        titlePerim.setTextFill(Color.GRAY);
        titlePerim.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));

        box.getChildren().addAll(titleArea, lblAreaValue, new Separator(), titlePerim, lblPerimeterValue);
        return box;
    }

    private VBox createInfoSection(String title, String[][] data) {
        VBox section = new VBox(10);

        // Título da Seção
        Label lblTitle = new Label(title);
        lblTitle.setTextFill(Color.web("#0078D7")); // Azul
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));

        // Grid para alinhar Chave vs Valor
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);

        for (int i = 0; i < data.length; i++) {
            String key = data[i][0];
            String value = data[i][1] != null ? data[i][1] : "-"; // Trata null

            // Label da Chave (Esquerda)
            Label lblKey = new Label(key + ":");
            lblKey.setTextFill(Color.GRAY);
            lblKey.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
            lblKey.setMinWidth(80); // Largura fixa para alinhar

            // Label do Valor (Direita)
            Label lblValue = new Label(value);
            lblValue.setTextFill(Color.WHITE);
            lblValue.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
            lblValue.setWrapText(true); // Quebra linha se for muito longo

            grid.add(lblKey, 0, i);
            grid.add(lblValue, 1, i);
        }

        section.getChildren().addAll(lblTitle, grid, new Separator());
        return section;
    }

    private VBox createProfessionalSection() {
        // Proteção de lista
        if (projectData.getResponsaveisTecnicos() == null) {
            projectData.setResponsaveisTecnicos(new ArrayList<>());
        }
        if (projectData.getResponsaveisTecnicos().isEmpty()) {
            projectData.getResponsaveisTecnicos().add(new ProfessionalSignature("-", "-", "-"));
        }

        ProfessionalSignature resp = projectData.getResponsaveisTecnicos().get(0);

        return createInfoSection("RESPONSÁVEL TÉCNICO", new String[][] {
                {"Nome", resp.getNome()},
                {"Formação", resp.getFormacaoTecnica()},
                {"Registro", resp.getRegistroClass()}
        });
    }

    public void updateMetrics(double areaHa, double perimeterM) {
        lblAreaValue.setText(String.format("%.4f ha", areaHa));
        lblPerimeterValue.setText(String.format("%.2f m", perimeterM));

        projectData.getPropertyInfo().setAreaHa(areaHa);
        projectData.getPropertyInfo().setPerimetroM(perimeterM);
    }

    /**
     * Recria a UI inteira. Útil se você editar os dados em outra tela
     * e quiser que essa barra reflita as mudanças de texto.
     */
    public void refreshData() {
        contentBox.getChildren().clear();
        initializeUI();

        // Forma correta de refresh rápido:
        this.setContent(null);
        initializeUI();
    }
}