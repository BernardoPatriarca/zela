package br.edu.unimater.zela.prestacaocontas.dto;

import io.quarkus.qute.TemplateData;

@TemplateData
public record LinhaConsolidadoDto(
        String titulo,
        String receitas,
        String despesas,
        String saldoFinal
) {}
