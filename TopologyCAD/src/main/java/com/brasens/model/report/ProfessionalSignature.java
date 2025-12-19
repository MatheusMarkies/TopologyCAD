package com.brasens.model.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProfessionalSignature {
    private String nome;
    private String formacaoTecnica; // Ex: "Engenheiro Agrimensor"
    private String registroClass;   // Ex: "CREA: 123456" ou "CPF: ..."
}