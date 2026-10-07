package br.edu.unimater.zela.usuario.dto;

import br.edu.unimater.zela.usuario.Usuario;

import java.time.Instant;
import java.util.UUID;

public record UsuarioResponseDto(
        UUID id,
        String nome,
        String email,
        Instant criadoEm,
        Instant atualizadoEm
) {
    public static UsuarioResponseDto from(Usuario usuario) {
        return new UsuarioResponseDto(
                usuario.id,
                usuario.nome,
                usuario.email,
                usuario.criadoEm,
                usuario.atualizadoEm
        );
    }
}