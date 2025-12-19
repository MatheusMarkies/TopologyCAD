package com.brasens.layout.components;

import com.brasens.model.report.ProfessionalSignature;
import com.brasens.model.report.ProjectData;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.StageStyle;

/**
 * Janela modal para edição dos dados do projeto.
 */
public class ProjectPropertiesDialog extends Dialog<Boolean> {

    private final ProjectData projectData;

    // Campos - Imóvel
    private TextField txtNomePropriedade = new TextField();
    private TextField txtProprietario = new TextField();
    private TextField txtMunicipio = new TextField();
    private TextField txtComarca = new TextField();
    private TextField txtCartorio = new TextField();
    private TextField txtMatricula = new TextField();
    private TextField txtIncra = new TextField();

    // Campos - Técnico
    private TextField txtDatum = new TextField();
    private TextField txtSistCoord = new TextField();
    private TextField txtMeridiano = new TextField();
    private TextField txtEquipamento = new TextField();
    private TextField txtEscala = new TextField();

    // Campos - Responsável (Editando o primeiro da lista)
    private TextField txtRespNome = new TextField();
    private TextField txtRespFormacao = new TextField();
    private TextField txtRespRegistro = new TextField();

    public ProjectPropertiesDialog(ProjectData data) {
        this.projectData = data;

        // Configuração da Janela
        this.setTitle("Propriedades do Projeto");
        this.setHeaderText("Edite as informações cadastrais e técnicas.");
        this.initStyle(StageStyle.UTILITY); // Estilo de janela de ferramentas

        // Botões (OK e Cancelar)
        ButtonType btnSaveType = new ButtonType("Salvar", ButtonBar.ButtonData.OK_DONE);
        this.getDialogPane().getButtonTypes().addAll(btnSaveType, ButtonType.CANCEL);

        // Layout com Abas
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        tabPane.getTabs().add(createPropertyTab());
        tabPane.getTabs().add(createTechnicalTab());
        tabPane.getTabs().add(createProfessionalTab());

        this.getDialogPane().setContent(tabPane);

        loadCurrentData();

        this.setResultConverter(dialogButton -> {
            if (dialogButton == btnSaveType) {
                saveData();
                return true;
            }
            return false;
        });
    }

    private Tab createPropertyTab() {
        GridPane grid = createGrid();
        addInput(grid, "Nome Propriedade:", txtNomePropriedade, 0);
        addInput(grid, "Proprietário:", txtProprietario, 1);
        addInput(grid, "Município:", txtMunicipio, 2);
        addInput(grid, "Comarca:", txtComarca, 3);
        addInput(grid, "Cartório:", txtCartorio, 4);
        addInput(grid, "Matrícula:", txtMatricula, 5);
        addInput(grid, "Cód. INCRA:", txtIncra, 6);
        return new Tab("Dados do Imóvel", grid);
    }

    private Tab createTechnicalTab() {
        GridPane grid = createGrid();
        addInput(grid, "Datum:", txtDatum, 0);
        addInput(grid, "Sist. Coordenadas:", txtSistCoord, 1);
        addInput(grid, "Meridiano Central:", txtMeridiano, 2);
        addInput(grid, "Equipamento:", txtEquipamento, 3);
        addInput(grid, "Escala Texto:", txtEscala, 4);
        return new Tab("Especificações", grid);
    }

    private Tab createProfessionalTab() {
        GridPane grid = createGrid();
        grid.add(new Label("Responsável Técnico Principal"), 0, 0, 2, 1);
        addInput(grid, "Nome Completo:", txtRespNome, 1);
        addInput(grid, "Formação:", txtRespFormacao, 2);
        addInput(grid, "Registro (CREA/CFT):", txtRespRegistro, 3);
        return new Tab("Responsável", grid);
    }

    private void loadCurrentData() {
        // Imóvel
        txtNomePropriedade.setText(projectData.getPropertyInfo().getNomePropriedade());
        txtProprietario.setText(projectData.getPropertyInfo().getProprietario());
        txtMunicipio.setText(projectData.getPropertyInfo().getMunicipio());
        txtComarca.setText(projectData.getPropertyInfo().getComarca());
        txtCartorio.setText(projectData.getPropertyInfo().getCartorio());
        txtMatricula.setText(projectData.getPropertyInfo().getMatricula());
        txtIncra.setText(projectData.getPropertyInfo().getCodigoIncra());

        // Técnico
        txtDatum.setText(projectData.getTechnicalSpecs().getDatum());
        txtSistCoord.setText(projectData.getTechnicalSpecs().getSistemaCoordenadas());
        txtMeridiano.setText(projectData.getTechnicalSpecs().getMeridianoCentral());
        txtEquipamento.setText(projectData.getTechnicalSpecs().getEquipamento());
        txtEscala.setText(projectData.getTechnicalSpecs().getEscalaTexto());

        // Responsável (Pega o primeiro com segurança)
        if (!projectData.getResponsaveisTecnicos().isEmpty()) {
            ProfessionalSignature resp = projectData.getResponsaveisTecnicos().get(0);
            txtRespNome.setText(resp.getNome());
            txtRespFormacao.setText(resp.getFormacaoTecnica());
            txtRespRegistro.setText(resp.getRegistroClass());
        }
    }

    private void saveData() {
        // Imóvel
        projectData.getPropertyInfo().setNomePropriedade(txtNomePropriedade.getText());
        projectData.getPropertyInfo().setProprietario(txtProprietario.getText());
        projectData.getPropertyInfo().setMunicipio(txtMunicipio.getText());
        projectData.getPropertyInfo().setComarca(txtComarca.getText());
        projectData.getPropertyInfo().setCartorio(txtCartorio.getText());
        projectData.getPropertyInfo().setMatricula(txtMatricula.getText());
        projectData.getPropertyInfo().setCodigoIncra(txtIncra.getText());

        // Técnico
        projectData.getTechnicalSpecs().setDatum(txtDatum.getText());
        projectData.getTechnicalSpecs().setSistemaCoordenadas(txtSistCoord.getText());
        projectData.getTechnicalSpecs().setMeridianoCentral(txtMeridiano.getText());
        projectData.getTechnicalSpecs().setEquipamento(txtEquipamento.getText());
        projectData.getTechnicalSpecs().setEscalaTexto(txtEscala.getText());

        // Responsável
        if (projectData.getResponsaveisTecnicos().isEmpty()) {
            projectData.getResponsaveisTecnicos().add(new ProfessionalSignature());
        }
        ProfessionalSignature resp = projectData.getResponsaveisTecnicos().get(0);
        resp.setNome(txtRespNome.getText());
        resp.setFormacaoTecnica(txtRespFormacao.getText());
        resp.setRegistroClass(txtRespRegistro.getText());
    }

    // Helpers UI
    private GridPane createGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new Insets(20));
        return grid;
    }

    private void addInput(GridPane grid, String label, TextField field, int row) {
        grid.add(new Label(label), 0, row);
        grid.add(field, 1, row);
    }
}