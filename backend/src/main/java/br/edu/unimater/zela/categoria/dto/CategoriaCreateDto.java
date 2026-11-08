package br.edu.unimater.zela.categoria.dto;

import br.edu.unimater.zela.categoria.TipoCategoria;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CategoriaCreateDto(
        @NotBlank(message = "O nome é obrigatório.")
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres.")
        String nome,

        @NotNull(message = "O tipo é obrigatório.")
        TipoCategoria tipo
) {}
