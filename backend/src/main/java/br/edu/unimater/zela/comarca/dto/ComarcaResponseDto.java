package br.edu.unimater.zela.comarca.dto;

import br.edu.unimater.zela.comarca.Comarca;

import java.time.Instant;
import java.util.UUID;

public record ComarcaResponseDto(
        UUID id,
        String nome,
        String uf,
        boolean sistema,
        Instant criadoEm,
        Instant atualizadoEm
) {
    public static ComarcaResponseDto from(Comarca comarca) {
        return new ComarcaResponseDto(
                comarca.id,
                comarca.nome,
                comarca.uf,
                comarca.isSistema(),
                comarca.criadoEm,
                comarca.atualizadoEm
        );
    }
}
