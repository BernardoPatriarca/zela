package br.edu.unimater.zela.prestacaocontas.dto;

import br.edu.unimater.zela.prestacaocontas.TipoPendencia;

import java.math.BigDecimal;

public record PendenciaDto(
        TipoPendencia tipo,
        String referencia,
        String texto,
        BigDecimal valor,
        String justificativa
) {}
