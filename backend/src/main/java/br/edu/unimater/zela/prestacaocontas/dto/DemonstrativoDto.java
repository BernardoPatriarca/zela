package br.edu.unimater.zela.prestacaocontas.dto;

import java.math.BigDecimal;

public record DemonstrativoDto(
        DocumentoPrestacaoDto documento,
        BigDecimal saldoFinalTotal
) {}
