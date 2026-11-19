package br.edu.unimater.zela.prestacaocontas.dto;

import br.edu.unimater.zela.prestacaocontas.TipoPendencia;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record JustificativaRequestDto(
        @NotNull(message = "O tipo da pendência é obrigatório.")
        TipoPendencia tipo,

        @NotBlank(message = "A referência da pendência é obrigatória.")
        @Size(max = 200, message = "A referência deve ter no máximo 200 caracteres.")
        String referencia,

        @Size(max = 1000, message = "A justificativa deve ter no máximo 1000 caracteres.")
        String texto
) {}
