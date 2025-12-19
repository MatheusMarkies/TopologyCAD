package com.brasens.model;

import javafx.scene.image.Image;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ProjectData {

    private PropertyMetadata propertyInfo;
    private TechnicalSpecs technicalSpecs;
    private List<ProfessionalSignature> responsaveisTecnicos;
    private ProfessionalSignature proprietarioAssinatura;

    private Image plantaLocalizacao;

    public ProjectData() {
        this.propertyInfo = new PropertyMetadata();
        this.technicalSpecs = new TechnicalSpecs();
        this.responsaveisTecnicos = new ArrayList<ProfessionalSignature>();
        this.proprietarioAssinatura = new ProfessionalSignature();

        this.technicalSpecs.setDatum("SIRGAS2000");
        this.technicalSpecs.setSistemaCoordenadas("UTM");
    }
}