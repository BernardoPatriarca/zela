package br.edu.unimater.zela.contabancaria.dto;

import br.edu.unimater.zela.contabancaria.Banco;
import br.edu.unimater.zela.contabancaria.TipoConta;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ContaBancariaUpdateDto(
        @NotNull(message = "O banco é obrigatório.")
        Banco banco,

        @NotNull(message = "O tipo de conta é obrigatório.")
        TipoConta tipo,

        @NotBlank(message = "A agência é obrigatória.")
        @Pattern(regexp = "^[0-9]{1,8}(-[0-9Xx])?$", message = "Agência inválida.")
        String agencia,

        @NotBlank(message = "O número da conta é obrigatório.")
        @Pattern(regexp = "^[0-9]{1,18}(-[0-9Xx])?$", message = "Número da conta inválido.")
        String numero,

        @Size(max = 100, message = "A descrição deve ter no máximo 100 caracteres.")
        String descricao,

        @NotNull(message = "O saldo inicial é obrigatório.")
        @Digits(integer = 13, fraction = 2, message = "O saldo inicial deve ter no máximo 2 casas decimais.")
        BigDecimal saldoInicial,

        @NotNull(message = "A data do saldo inicial é obrigatória.")
        @PastOrPresent(message = "A data do saldo inicial não pode ser futura.")
        LocalDate dataSaldoInicial
) {}
