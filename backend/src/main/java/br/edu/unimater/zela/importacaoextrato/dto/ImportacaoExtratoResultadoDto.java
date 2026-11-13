package br.edu.unimater.zela.importacaoextrato.dto;

import java.util.List;

public record ImportacaoExtratoResultadoDto(
        ImportacaoExtratoResponseDto importacao,
        int conciliadasAutomaticamente,
        List<String> avisos
) {}
