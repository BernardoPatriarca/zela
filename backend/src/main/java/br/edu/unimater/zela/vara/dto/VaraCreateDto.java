package br.edu.unimater.zela.vara.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record VaraCreateDto(
        @NotNull(message = "A comarca é obrigatória.")
        UUID comarcaId,

        @NotBlank(message = "O nome é obrigatório.")
        @Size(max = 150, message = "O nome deve ter no máximo 150 caracteres.")
        String nome
) {}
