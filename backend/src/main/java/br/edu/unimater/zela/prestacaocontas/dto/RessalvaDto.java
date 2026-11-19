package br.edu.unimater.zela.prestacaocontas.dto;

import io.quarkus.qute.TemplateData;

@TemplateData
public record RessalvaDto(
        String texto,
        String justificativa
) {}
