package br.edu.unimater.zela.prestacaocontas.dto;

import io.quarkus.qute.TemplateData;

@TemplateData
public record LinhaPlanilhaDto(
        String numero,
        String data,
        String emitente,
        String historico,
        String classificacao,
        String credito,
        String debito,
        String saldo
) {}
