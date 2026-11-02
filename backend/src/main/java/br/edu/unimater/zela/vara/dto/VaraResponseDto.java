package br.edu.unimater.zela.vara.dto;

import br.edu.unimater.zela.vara.Vara;

import java.time.Instant;
import java.util.UUID;

public record VaraResponseDto(
        UUID id,
        String nome,
        UUID comarcaId,
        String comarcaNome,
        String comarcaUf,
        boolean sistema,
        Instant criadoEm,
        Instant atualizadoEm
) {
    public static VaraResponseDto from(Vara vara) {
        return new VaraResponseDto(
                vara.id,
                vara.nome,
                vara.comarca.id,
                vara.comarca.nome,
                vara.comarca.uf,
                vara.isSistema(),
                vara.criadoEm,
                vara.atualizadoEm
        );
    }
}
