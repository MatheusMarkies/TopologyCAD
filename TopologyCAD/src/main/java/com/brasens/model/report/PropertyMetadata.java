package com.brasens.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PropertyMetadata {
    private String nomePropriedade;
    private String proprietario;
    private String municipio;
    private String comarca;
    private String cartorio;
    private String matricula;     // "MAT./TRANSC."
    private String codigoIncra;

    // Estes valores geralmente vêm do cálculo do TopoObject,
    // mas podem ser sobrescritos manualmente se necessário.
    private double areaHa;
    private double perimetroM;

    private LocalDate dataLevantamento;
}