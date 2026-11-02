package br.edu.unimater.zela.comarca.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ComarcaUpdateDto(
        @NotBlank(message = "O nome é obrigatório.")
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres.")
        String nome,

        @NotBlank(message = "A UF é obrigatória.")
        @Pattern(regexp = "^[A-Za-z]{2}$", message = "UF inválida.")
        String uf
) {}
