package br.edu.unimater.zela.usuario.dto;

import br.edu.unimater.zela.usuario.Usuario;

import java.time.Instant;
import java.util.UUID;

public record UsuarioResponseDto(
        UUID id,
        String nome,
        String email,
        String nomeCompleto,
        String oabNumero,
        String oabUf,
        Instant criadoEm,
        Instant atualizadoEm
) {
    public static UsuarioResponseDto from(Usuario usuario) {
        return new UsuarioResponseDto(
                usuario.id,
                usuario.nome,
                usuario.email,
                usuario.nomeCompleto,
                usuario.oabNumero,
                usuario.oabUf,
                usuario.criadoEm,
                usuario.atualizadoEm
        );
    }
}