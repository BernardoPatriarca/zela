package br.edu.unimater.zela.conciliacao.dto;

public record ConciliacaoAutomaticaResultadoDto(
        int conciliadas,
        int pendentes,
        int ambiguas
) {}
