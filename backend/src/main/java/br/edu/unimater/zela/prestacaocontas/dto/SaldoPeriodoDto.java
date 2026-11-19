package br.edu.unimater.zela.prestacaocontas.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record SaldoPeriodoDto(
        UUID contaBancariaId,
        String contaDescricao,
        BigDecimal saldoInicial,
        BigDecimal saldoSugerido,
        BigDecimal saldoFinal
) {}
