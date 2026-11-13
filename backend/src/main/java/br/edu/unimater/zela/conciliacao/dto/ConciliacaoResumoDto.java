package br.edu.unimater.zela.conciliacao.dto;

public record ConciliacaoResumoDto(
        long pendentes,
        long conciliadas,
        long ignoradas
) {}
