package br.edu.unimater.zela.caso.dto;

import br.edu.unimater.zela.caso.TipoCaso;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record CasoCreateDto(
        @NotNull(message = "O tipo é obrigatório.")
        TipoCaso tipo,

        @NotBlank(message = "O número do processo é obrigatório.")
        String numeroProcesso,

        @NotNull(message = "A vara é obrigatória.")
        UUID varaId,

        @NotNull(message = "A pessoa protegida é obrigatória.")
        UUID pessoaProtegidaId,

        @NotNull(message = "O responsável é obrigatório.")
        UUID responsavelId,

        @NotNull(message = "A periodicidade é obrigatória.")
        @Min(value = 1, message = "A periodicidade deve ser de pelo menos 1 mês.")
        @Max(value = 60, message = "A periodicidade deve ser de no máximo 60 meses.")
        Integer periodicidadeMeses,

        @NotNull(message = "O prazo de entrega é obrigatório.")
        @Min(value = 0, message = "O prazo de entrega não pode ser negativo.")
        @Max(value = 365, message = "O prazo de entrega deve ser de no máximo 365 dias.")
        Integer prazoEntregaDias,

        @NotNull(message = "O início do período é obrigatório.")
        LocalDate inicioPeriodo
) {}
