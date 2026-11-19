package br.edu.unimater.zela.prestacaocontas.dto;

import io.quarkus.qute.TemplateData;

@TemplateData
public record ConferenciaAritmeticaDto(
        String titulo,
        String saldoInicial,
        String receitas,
        String despesas,
        String saldoFinal
) {}
