package br.edu.unimater.zela.lancamento.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LancamentoExclusaoDto(
        @NotBlank(message = "A justificativa é obrigatória.")
        @Size(min = 10, max = 500, message = "A justificativa deve ter entre 10 e 500 caracteres.")
        String justificativa
) {}
