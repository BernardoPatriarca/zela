package br.edu.unimater.zela.prestacaocontas.dto;

import br.edu.unimater.zela.caso.Caso;
import br.edu.unimater.zela.caso.PeriodoPrestacao;
import br.edu.unimater.zela.caso.SituacaoPeriodo;
import br.edu.unimater.zela.caso.TipoCaso;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record PrestacaoContasDetalheDto(
        UUID id,
        UUID casoId,
        TipoCaso tipo,
        String numeroProcesso,
        String pessoaProtegidaNome,
        String responsavelNome,
        boolean casoEncerrado,
        LocalDate dataInicio,
        LocalDate dataFim,
        SituacaoPeriodo situacao,
        LocalDate prazoCurador,
        LocalDate prazoEntrega,
        LocalDate entregueEm,
        Boolean entregueNoPrazo,
        boolean vencido,
        String notasExplicativas,
        List<SaldoPeriodoDto> saldos,
        List<PendenciaDto> pendencias,
        List<RelatorioResponseDto> relatorios,
        UUID relatorioEntregueId
) {
    public static PrestacaoContasDetalheDto from(PeriodoPrestacao periodo, Caso caso, LocalDate hoje,
                                                 List<SaldoPeriodoDto> saldos, List<PendenciaDto> pendencias,
                                                 List<RelatorioResponseDto> relatorios) {
        UUID relatorioEntregueId = periodo.isEntregue() && !relatorios.isEmpty() ? relatorios.get(0).id() : null;
        return new PrestacaoContasDetalheDto(
                periodo.id,
                caso.id,
                caso.tipo,
                caso.numeroProcesso,
                caso.pessoaProtegida.nome,
                caso.responsavel.nome,
                caso.isEncerrado(),
                periodo.dataInicio,
                periodo.dataFim,
                periodo.situacao,
                periodo.prazoCurador,
                periodo.prazoEntrega,
                periodo.entregueEm,
                periodo.entregueEm == null ? null : !periodo.entregueEm.isAfter(periodo.prazoEntrega),
                periodo.entregueEm == null && periodo.prazoEntrega.isBefore(hoje),
                periodo.notasExplicativas,
                saldos,
                pendencias,
                relatorios,
                relatorioEntregueId
        );
    }
}
