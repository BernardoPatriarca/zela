package br.edu.unimater.zela.prestacaocontas.dto;

import io.quarkus.qute.TemplateData;

@TemplateData
public record ConciliacaoContaDto(
        String titulo,
        boolean comExtrato,
        String dataExtrato,
        String saldoCalculado,
        String saldoExtrato,
        String diferenca
) {}
