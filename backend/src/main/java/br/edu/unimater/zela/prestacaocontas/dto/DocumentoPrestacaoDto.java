package br.edu.unimater.zela.prestacaocontas.dto;

import io.quarkus.qute.TemplateData;

import java.util.List;

@TemplateData
public record DocumentoPrestacaoDto(
        String varaMaiusculo,
        String comarcaMaiusculo,
        String juizo,
        String autos,
        String rotuloProtegido,
        String protegido,
        String cpfProtegido,
        String rotuloResponsavel,
        String responsavel,
        String cpfResponsavel,
        String advogado,
        String oab,
        String periodo,
        String periodicidade,
        String fundamentacao,
        String saldoFinal,
        boolean comRessalvas,
        String local,
        String dataPeticao,
        List<PlanilhaContaDto> planilhas,
        List<LinhaConsolidadoDto> consolidado,
        LinhaConsolidadoDto totalConsolidado,
        List<ConferenciaAritmeticaDto> conferencias,
        List<ConciliacaoContaDto> conciliacoes,
        List<RessalvaDto> ressalvas,
        String notasExplicativas
) {}
