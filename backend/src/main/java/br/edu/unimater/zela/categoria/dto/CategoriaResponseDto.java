package br.edu.unimater.zela.categoria.dto;

import br.edu.unimater.zela.categoria.Categoria;
import br.edu.unimater.zela.categoria.TipoCategoria;

import java.time.Instant;
import java.util.UUID;

public record CategoriaResponseDto(
        UUID id,
        String nome,
        TipoCategoria tipo,
        boolean sistema,
        Instant criadoEm,
        Instant atualizadoEm
) {
    public static CategoriaResponseDto from(Categoria categoria) {
        return new CategoriaResponseDto(
                categoria.id,
                categoria.nome,
                categoria.tipo,
                categoria.isSistema(),
                categoria.criadoEm,
                categoria.atualizadoEm
        );
    }
}
