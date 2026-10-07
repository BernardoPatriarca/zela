package br.edu.unimater.zela.usuario.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UsuarioUpdateDto(
        @NotBlank(message = "O nome é obrigatório.")
        @Size(max = 150, message = "O nome deve ter no máximo 150 caracteres.")
        String nome,

        @NotBlank(message = "O e-mail é obrigatório.")
        @Size(max = 255, message = "O e-mail deve ter no máximo 255 caracteres.")
        @Email(message = "E-mail inválido.")
        String email
) {}