package br.edu.unimater.zela.prestacaocontas.dto;

import br.edu.unimater.zela.caso.Caso;
import br.edu.unimater.zela.caso.PeriodoPrestacao;
import br.edu.unimater.zela.caso.SituacaoPeriodo;
import br.edu.unimater.zela.caso.TipoCaso;

import java.time.LocalDate;
import java.util.UUID;

public record PrestacaoContasResumoDto(
        UUID id,
        UUID casoId,
        TipoCaso tipo,
        String numeroProcesso,
        String pessoaProtegidaNome,
        boolean casoEncerrado,
        LocalDate dataInicio,
        LocalDate dataFim,
        SituacaoPeriodo situacao,
        LocalDate prazoCurador,
        LocalDate prazoEntrega,
        LocalDate entregueEm,
        Boolean entregueNoPrazo,
        boolean vencido
) {
    public static PrestacaoContasResumoDto from(PeriodoPrestacao periodo, Caso caso, LocalDate hoje) {
        return new PrestacaoContasResumoDto(
                periodo.id,
                caso.id,
                caso.tipo,
                caso.numeroProcesso,
                caso.pessoaProtegida.nome,
                caso.isEncerrado(),
                periodo.dataInicio,
                periodo.dataFim,
                periodo.situacao,
                periodo.prazoCurador,
                periodo.prazoEntrega,
                periodo.entregueEm,
                periodo.entregueEm == null ? null : !periodo.entregueEm.isAfter(periodo.prazoEntrega),
                periodo.entregueEm == null && periodo.prazoEntrega.isBefore(hoje)
        );
    }
}
