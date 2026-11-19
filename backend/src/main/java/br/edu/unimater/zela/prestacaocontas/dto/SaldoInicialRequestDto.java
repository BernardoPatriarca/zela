package br.edu.unimater.zela.prestacaocontas.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record SaldoInicialRequestDto(
        @NotNull(message = "A conta bancária é obrigatória.")
        UUID contaBancariaId,

        @NotNull(message = "O saldo inicial é obrigatório.")
        @Digits(integer = 13, fraction = 2, message = "O saldo inicial deve ter no máximo 2 casas decimais.")
        BigDecimal saldoInicial
) {}
