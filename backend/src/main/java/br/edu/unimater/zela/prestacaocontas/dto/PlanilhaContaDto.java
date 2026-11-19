package br.edu.unimater.zela.prestacaocontas.dto;

import io.quarkus.qute.TemplateData;

import java.util.List;

@TemplateData
public record PlanilhaContaDto(
        String titulo,
        List<MesPlanilhaDto> meses
) {}
