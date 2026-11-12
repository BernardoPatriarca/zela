package br.edu.unimater.zela.banco.dto;

import br.edu.unimater.zela.banco.Banco;

import java.time.Instant;
import java.util.UUID;

public record BancoResponseDto(
        UUID id,
        String codigo,
        String nome,
        boolean sistema,
        Instant criadoEm,
        Instant atualizadoEm
) {
    public static BancoResponseDto from(Banco banco) {
        return new BancoResponseDto(
                banco.id,
                banco.codigo,
                banco.nome,
                banco.isSistema(),
                banco.criadoEm,
                banco.atualizadoEm
        );
    }
}
