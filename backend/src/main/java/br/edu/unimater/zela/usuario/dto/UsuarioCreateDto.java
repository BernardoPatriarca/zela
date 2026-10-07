package br.edu.unimater.zela.usuario.dto;

import java.util.UUID;

public record UsuarioCreateDto(
        UUID id,
        String nome,
        String email
) {}