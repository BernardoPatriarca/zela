package br.edu.unimater.zela.prestacaocontas.dto;

import io.quarkus.qute.TemplateData;

import java.util.List;

@TemplateData
public record MesPlanilhaDto(
        String titulo,
        String saldoAnterior,
        List<LinhaPlanilhaDto> linhas,
        String totalCredito,
        String totalDebito,
        String saldoFinal
) {}
