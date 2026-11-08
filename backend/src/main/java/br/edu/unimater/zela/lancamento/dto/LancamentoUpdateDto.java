package br.edu.unimater.zela.lancamento.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record LancamentoUpdateDto(
        @NotNull(message = "A conta bancária é obrigatória.")
        UUID contaBancariaId,

        @NotNull(message = "A categoria é obrigatória.")
        UUID categoriaId,

        @NotNull(message = "A data do lançamento é obrigatória.")
        @PastOrPresent(message = "A data do lançamento não pode ser futura.")
        LocalDate dataLancamento,

        @NotNull(message = "O valor é obrigatório.")
        @Positive(message = "O valor deve ser maior que zero.")
        @Digits(integer = 13, fraction = 2, message = "O valor deve ter no máximo 2 casas decimais.")
        BigDecimal valor,

        @NotBlank(message = "A descrição é obrigatória.")
        @Size(max = 255, message = "A descrição deve ter no máximo 255 caracteres.")
        String descricao,

        @NotBlank(message = "A justificativa é obrigatória.")
        @Size(min = 10, max = 500, message = "A justificativa deve ter entre 10 e 500 caracteres.")
        String justificativa
) {}
