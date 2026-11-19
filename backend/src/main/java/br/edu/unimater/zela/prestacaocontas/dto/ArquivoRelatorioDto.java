package br.edu.unimater.zela.prestacaocontas.dto;

public record ArquivoRelatorioDto(
        String nome,
        byte[] conteudo
) {}
