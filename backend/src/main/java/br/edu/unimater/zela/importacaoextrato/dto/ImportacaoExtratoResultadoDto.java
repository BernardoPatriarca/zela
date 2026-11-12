package br.edu.unimater.zela.importacaoextrato.dto;

import java.util.List;

public record ImportacaoExtratoResultadoDto(
        ImportacaoExtratoResponseDto importacao,
        List<String> avisos
) {}
