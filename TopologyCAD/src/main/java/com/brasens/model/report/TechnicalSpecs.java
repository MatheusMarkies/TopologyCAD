package com.brasens.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TechnicalSpecs {
    // Contato
    private String emailContato;
    private String telefone1;
    private String telefone2;

    // Dados Técnicos
    private String sistemaCoordenadas; // Ex: "Planas UTM"
    private String datum;              // Ex: "SIRGAS2000"
    private String meridianoCentral;   // Ex: "45º WGR"
    private String equipamento;        // Ex: "GPS GNSS CHC - i80"

    // Dados do Desenho
    private String tituloDesenho;      // Ex: "Planialtimétrico Para Estudo"
    private String escalaTexto;        // Ex: "1 / 1800"
    private String numeroFolha;        // Ex: "01"
    private String tamanhoPapel;       // Ex: "A1 (841 x 594)"
}