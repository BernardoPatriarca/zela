package br.edu.unimater.zela.usuario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UsuarioDadosProfissionaisDto(
        @NotBlank(message = "O nome completo é obrigatório.")
        @Size(max = 150, message = "O nome completo deve ter no máximo 150 caracteres.")
        String nomeCompleto,

        @NotBlank(message = "O número da OAB é obrigatório.")
        @Pattern(regexp = "\\d{1,7}", message = "O número da OAB deve ter apenas dígitos.")
        String oabNumero,

        @NotBlank(message = "A UF da OAB é obrigatória.")
        @Pattern(regexp = "[A-Za-z]{2}", message = "A UF da OAB deve ter 2 letras.")
        String oabUf
) {}
