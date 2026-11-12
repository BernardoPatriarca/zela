package br.edu.unimater.zela.banco.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record BancoCreateDto(
        @NotBlank(message = "O código é obrigatório.")
        @Pattern(regexp = "^[0-9]{3}$", message = "O código deve ter 3 dígitos.")
        String codigo,

        @NotBlank(message = "O nome é obrigatório.")
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres.")
        String nome
) {}
