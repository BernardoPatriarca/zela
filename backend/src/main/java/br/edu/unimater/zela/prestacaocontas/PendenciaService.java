package br.edu.unimater.zela.prestacaocontas;

import br.edu.unimater.zela.caso.Caso;
import br.edu.unimater.zela.caso.PeriodoPrestacao;
import br.edu.unimater.zela.caso.PeriodoPrestacaoRepository;
import br.edu.unimater.zela.categoria.TipoCategoria;
import br.edu.unimater.zela.contabancaria.ContaBancaria;
import br.edu.unimater.zela.importacaoextrato.ImportacaoExtrato;
import br.edu.unimater.zela.importacaoextrato.ImportacaoExtratoRepository;
import br.edu.unimater.zela.importacaoextrato.MovimentacaoExtrato;
import br.edu.unimater.zela.importacaoextrato.MovimentacaoExtratoRepository;
import br.edu.unimater.zela.lancamento.Lancamento;
import br.edu.unimater.zela.prestacaocontas.dto.PendenciaDto;
import br.edu.unimater.zela.shared.util.Formato;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@ApplicationScoped
public class PendenciaService {

    @Inject
    MovimentacaoExtratoRepository movimentacaoRepository;

    @Inject
    ImportacaoExtratoRepository importacaoRepository;

    @Inject
    PeriodoPrestacaoRepository periodoRepository;

    @Inject
    SaldoPeriodoRepository saldoRepository;

    @Inject
    JustificativaPendenciaRepository justificativaRepository;

    @Inject
    SaldoService saldoService;

    public List<PendenciaDto> listar(Caso caso, PeriodoPrestacao periodo, DadosPeriodo dados) {
        Map<String, String> justificativas = new HashMap<>();
        for (JustificativaPendencia justificativa : justificativaRepository.listarPorPeriodo(periodo.id)) {
            justificativas.put(justificativa.tipo + "|" + justificativa.referencia, justificativa.texto);
        }

        return calcular(caso, periodo, dados).stream()
                .map(pendencia -> new PendenciaDto(pendencia.tipo(), pendencia.referencia(), pendencia.texto(),
                        pendencia.valor(), justificativas.get(pendencia.tipo() + "|" + pendencia.referencia())))
                .toList();
    }

    private List<PendenciaDto> calcular(Caso caso, PeriodoPrestacao periodo, DadosPeriodo dados) {
        List<PendenciaDto> pendencias = new ArrayList<>();
        if (dados.contas().isEmpty()) {
            return pendencias;
        }
        List<UUID> contasIds = dados.contas().stream().map(conta -> conta.id).toList();

        movimentacoesPendentes(periodo, contasIds, pendencias);
        lancamentosSemExtrato(periodo, contasIds, dados, pendencias);
        extratoFaltando(periodo, contasIds, dados, pendencias);
        saldosDivergentes(periodo, contasIds, dados, pendencias);
        saldosIniciaisDiferentes(caso, periodo, dados, pendencias);
        possiveisDuplicidades(dados, pendencias);
        return pendencias;
    }

    private void movimentacoesPendentes(PeriodoPrestacao periodo, List<UUID> contasIds,
                                        List<PendenciaDto> pendencias) {
        for (MovimentacaoExtrato movimentacao : movimentacaoRepository.listarPendentesDoPeriodo(contasIds,
                periodo.dataInicio, periodo.dataFim)) {
            pendencias.add(new PendenciaDto(TipoPendencia.MOVIMENTACAO_PENDENTE, movimentacao.id.toString(),
                    "Movimentação de " + Formato.moeda(movimentacao.valor) + " em "
                            + Formato.data(movimentacao.dataMovimentacao) + " na conta "
                            + movimentacao.contaBancaria.numero + " sem lançamento correspondente: "
                            + movimentacao.descricao + ".",
                    movimentacao.valor, null));
        }
    }

    private void lancamentosSemExtrato(PeriodoPrestacao periodo, List<UUID> contasIds, DadosPeriodo dados,
                                       List<PendenciaDto> pendencias) {
        Set<UUID> conciliados = new HashSet<>(movimentacaoRepository.listarLancamentosConciliadosDoPeriodo(
                contasIds, periodo.dataInicio, periodo.dataFim));
        for (Lancamento lancamento : dados.lancamentos()) {
            if (conciliados.contains(lancamento.id)) {
                continue;
            }
            pendencias.add(new PendenciaDto(TipoPendencia.LANCAMENTO_SEM_EXTRATO, lancamento.id.toString(),
                    "Lançamento de " + Formato.moeda(lancamento.valor) + " em "
                            + Formato.data(lancamento.dataLancamento) + " na conta "
                            + lancamento.contaBancaria.numero + " não está ligado a nenhuma movimentação do extrato: "
                            + lancamento.descricao + ".",
                    lancamento.valor, null));
        }
    }

    private void extratoFaltando(PeriodoPrestacao periodo, List<UUID> contasIds, DadosPeriodo dados,
                                 List<PendenciaDto> pendencias) {
        Map<UUID, List<ImportacaoExtrato>> importacoesPorConta = new HashMap<>();
        for (ImportacaoExtrato importacao : importacaoRepository.listarQueTocamPeriodo(contasIds,
                periodo.dataInicio, periodo.dataFim)) {
            importacoesPorConta.computeIfAbsent(importacao.contaBancaria.id, id -> new ArrayList<>())
                    .add(importacao);
        }

        for (ContaBancaria conta : dados.contas()) {
            LocalDate inicio = conta.dataSaldoInicial.isAfter(periodo.dataInicio)
                    ? conta.dataSaldoInicial
                    : periodo.dataInicio;
            if (inicio.isAfter(periodo.dataFim)) {
                continue;
            }

            List<String> buracos = new ArrayList<>();
            LocalDate cursor = inicio;
            for (ImportacaoExtrato importacao : importacoesPorConta.getOrDefault(conta.id, List.of())) {
                if (cursor.isAfter(periodo.dataFim)) {
                    break;
                }
                if (importacao.dataInicio.isAfter(cursor)) {
                    LocalDate ate = importacao.dataInicio.minusDays(1);
                    if (ate.isAfter(periodo.dataFim)) {
                        ate = periodo.dataFim;
                    }
                    buracos.add(Formato.data(cursor) + " a " + Formato.data(ate));
                }
                if (importacao.dataFim.plusDays(1).isAfter(cursor)) {
                    cursor = importacao.dataFim.plusDays(1);
                }
            }
            if (!cursor.isAfter(periodo.dataFim)) {
                buracos.add(Formato.data(cursor) + " a " + Formato.data(periodo.dataFim));
            }

            if (!buracos.isEmpty()) {
                pendencias.add(new PendenciaDto(TipoPendencia.EXTRATO_FALTANDO, conta.id.toString(),
                        "Extrato da conta " + conta.numero + " sem cobertura em " + String.join("; ", buracos) + ".",
                        null, null));
            }
        }
    }

    private void saldosDivergentes(PeriodoPrestacao periodo, List<UUID> contasIds, DadosPeriodo dados,
                                   List<PendenciaDto> pendencias) {
        Map<UUID, ImportacaoExtrato> extratos = new HashMap<>();
        for (ImportacaoExtrato importacao : importacaoRepository.listarComSaldoNoPeriodo(contasIds,
                periodo.dataInicio, periodo.dataFim)) {
            extratos.putIfAbsent(importacao.contaBancaria.id, importacao);
        }

        for (ContaBancaria conta : dados.contas()) {
            BigDecimal saldoInicial = dados.saldosIniciais().get(conta.id);
            ImportacaoExtrato extrato = extratos.get(conta.id);
            if (saldoInicial == null || extrato == null) {
                continue;
            }
            BigDecimal calculado = saldoService.saldoEm(saldoInicial,
                    dados.lancamentosPorConta().getOrDefault(conta.id, List.of()), extrato.dataSaldoFinal);
            BigDecimal diferenca = extrato.saldoFinal.subtract(calculado).abs();
            if (diferenca.signum() != 0) {
                pendencias.add(new PendenciaDto(TipoPendencia.SALDO_DIVERGENTE, conta.id.toString(),
                        "Diferença de " + Formato.moeda(diferenca) + " entre o saldo calculado e o extrato da conta "
                                + conta.numero + " em " + Formato.data(extrato.dataSaldoFinal) + ".",
                        diferenca, null));
            }
        }
    }

    private void saldosIniciaisDiferentes(Caso caso, PeriodoPrestacao periodo, DadosPeriodo dados,
                                          List<PendenciaDto> pendencias) {
        Optional<PeriodoPrestacao> anterior = periodoRepository.buscarEntregueAnterior(caso.id, periodo.dataInicio);
        if (anterior.isEmpty()) {
            return;
        }

        Map<UUID, BigDecimal> saldosFinais = new HashMap<>();
        for (SaldoPeriodo saldo : saldoRepository.listarPorPeriodo(anterior.get().id)) {
            if (saldo.saldoFinal != null) {
                saldosFinais.put(saldo.contaBancariaId, saldo.saldoFinal);
            }
        }

        for (ContaBancaria conta : dados.contas()) {
            BigDecimal saldoInicial = dados.saldosIniciais().get(conta.id);
            BigDecimal saldoFinalAnterior = saldosFinais.get(conta.id);
            if (saldoInicial == null || saldoFinalAnterior == null
                    || saldoInicial.compareTo(saldoFinalAnterior) == 0) {
                continue;
            }
            pendencias.add(new PendenciaDto(TipoPendencia.SALDO_INICIAL_DIFERENTE, conta.id.toString(),
                    "O saldo inicial da conta " + conta.numero + " (" + Formato.moeda(saldoInicial)
                            + ") é diferente do saldo final do período anterior (" + Formato.moeda(saldoFinalAnterior)
                            + ").",
                    saldoInicial.subtract(saldoFinalAnterior).abs(), null));
        }
    }

    private void possiveisDuplicidades(DadosPeriodo dados, List<PendenciaDto> pendencias) {
        Map<String, List<Lancamento>> grupos = new LinkedHashMap<>();
        for (Lancamento lancamento : dados.lancamentos()) {
            String chave = lancamento.contaBancaria.id + "|" + lancamento.tipo + "|"
                    + lancamento.valor.setScale(2, RoundingMode.HALF_UP).toPlainString() + "|"
                    + lancamento.dataLancamento;
            grupos.computeIfAbsent(chave, k -> new ArrayList<>()).add(lancamento);
        }

        for (Map.Entry<String, List<Lancamento>> grupo : grupos.entrySet()) {
            if (grupo.getValue().size() < 2) {
                continue;
            }

            List<List<Lancamento>> conjuntos = new ArrayList<>();
            for (Lancamento lancamento : grupo.getValue()) {
                List<Lancamento> conjunto = conjuntos.stream()
                        .filter(existente -> existente.stream().anyMatch(outro -> duplicados(outro, lancamento)))
                        .findFirst()
                        .orElse(null);
                if (conjunto == null) {
                    conjunto = new ArrayList<>();
                    conjuntos.add(conjunto);
                }
                conjunto.add(lancamento);
            }

            for (List<Lancamento> conjunto : conjuntos) {
                if (conjunto.size() < 2) {
                    continue;
                }
                Lancamento primeiro = conjunto.get(0);
                pendencias.add(new PendenciaDto(TipoPendencia.POSSIVEL_DUPLICIDADE,
                        grupo.getKey() + "|" + discriminador(primeiro),
                        "Possível duplicidade: " + conjunto.size() + " lançamentos de "
                                + Formato.moeda(primeiro.valor) + " em " + Formato.data(primeiro.dataLancamento)
                                + " na conta " + primeiro.contaBancaria.numero + " (" + primeiro.descricao + ").",
                        primeiro.valor, null));
            }
        }
    }

    private boolean duplicados(Lancamento a, Lancamento b) {
        if (a.emitenteDocumento != null && b.emitenteDocumento != null) {
            return a.emitenteDocumento.equals(b.emitenteDocumento);
        }
        return normalizar(a.descricao).equals(normalizar(b.descricao));
    }

    private String discriminador(Lancamento lancamento) {
        if (lancamento.emitenteDocumento != null) {
            return "doc:" + lancamento.emitenteDocumento;
        }
        String descricao = normalizar(lancamento.descricao);
        return "desc:" + (descricao.length() > 60 ? descricao.substring(0, 60) : descricao);
    }

    private String normalizar(String texto) {
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .trim()
                .toLowerCase()
                .replaceAll("\\s+", " ");
    }
}
