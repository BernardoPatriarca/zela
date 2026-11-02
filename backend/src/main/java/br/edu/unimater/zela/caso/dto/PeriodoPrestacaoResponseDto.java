package br.edu.unimater.zela.caso.dto;

import br.edu.unimater.zela.caso.PeriodoPrestacao;

import java.time.LocalDate;
import java.util.UUID;

public record PeriodoPrestacaoResponseDto(
        UUID id,
        LocalDate dataInicio,
        LocalDate dataFim,
        LocalDate prazoEntrega
) {
    public static PeriodoPrestacaoResponseDto from(PeriodoPrestacao periodo) {
        if (periodo == null) {
            return null;
        }
        return new PeriodoPrestacaoResponseDto(periodo.id, periodo.dataInicio, periodo.dataFim, periodo.prazoEntrega);
    }
}
