package br.edu.unimater.zela.conciliacao;

import br.edu.unimater.zela.caso.Caso;
import br.edu.unimater.zela.caso.CasoRepository;
import br.edu.unimater.zela.categoria.TipoCategoria;
import br.edu.unimater.zela.conciliacao.dto.ConciliacaoAutomaticaResultadoDto;
import br.edu.unimater.zela.conciliacao.dto.ConciliacaoResumoDto;
import br.edu.unimater.zela.conciliacao.dto.MovimentacaoConciliacaoResponseDto;
import br.edu.unimater.zela.contabancaria.ContaBancaria;
import br.edu.unimater.zela.contabancaria.ContaBancariaRepository;
import br.edu.unimater.zela.importacaoextrato.FormaConciliacao;
import br.edu.unimater.zela.importacaoextrato.MovimentacaoExtrato;
import br.edu.unimater.zela.importacaoextrato.MovimentacaoExtratoRepository;
import br.edu.unimater.zela.importacaoextrato.SituacaoMovimentacao;
import br.edu.unimater.zela.lancamento.Lancamento;
import br.edu.unimater.zela.lancamento.LancamentoRepository;
import br.edu.unimater.zela.shared.dto.Pagina;
import br.edu.unimater.zela.shared.exception.BusinessRuleException;
import br.edu.unimater.zela.shared.exception.ResourceNotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class ConciliacaoService {

    @Inject
    MovimentacaoExtratoRepository repository;

    @Inject
    LancamentoRepository lancamentoRepository;

    @Inject
    CasoRepository casoRepository;

    @Inject
    ContaBancariaRepository contaBancariaRepository;

    @Inject
    JsonWebToken jwt;

    public Pagina<MovimentacaoConciliacaoResponseDto> listar(UUID casoId, UUID contaBancariaId, LocalDate dataInicio,
                                                             LocalDate dataFim, SituacaoMovimentacao situacao,
                                                             int pagina, int tamanho) {
        Caso caso = buscarCaso(casoId);
        validarPeriodo(dataInicio, dataFim);

        Pagina<MovimentacaoExtrato> movimentacoes = repository.listar(caso.id, contaBancariaId, dataInicio, dataFim,
                situacao, pagina, tamanho);
        Map<ChaveConciliacao, Long> candidatos = contarCandidatos(movimentacoes.itens());
        return movimentacoes.map(movimentacao -> MovimentacaoConciliacaoResponseDto.from(movimentacao,
                movimentacao.situacao == SituacaoMovimentacao.PENDENTE
                        ? candidatos.getOrDefault(ChaveConciliacao.de(movimentacao), 0L).intValue()
                        : 0));
    }

    public ConciliacaoResumoDto resumo(UUID casoId, UUID contaBancariaId, LocalDate dataInicio, LocalDate dataFim) {
        Caso caso = buscarCaso(casoId);
        validarPeriodo(dataInicio, dataFim);

        Map<SituacaoMovimentacao, Long> contagens = repository.contarPorSituacao(caso.id, contaBancariaId,
                dataInicio, dataFim);
        return new ConciliacaoResumoDto(
                contagens.get(SituacaoMovimentacao.PENDENTE),
                contagens.get(SituacaoMovimentacao.CONCILIADA),
                contagens.get(SituacaoMovimentacao.IGNORADA)
        );
    }

    @Transactional
    public ConciliacaoAutomaticaResultadoDto conciliarAutomaticamente(UUID casoId, UUID contaBancariaId) {
        Caso caso = buscarCasoEditavel(casoId);
        List<UUID> contasIds = contaBancariaId == null
                ? contaBancariaRepository.listarPorCaso(caso.id).stream().map(conta -> conta.id).toList()
                : List.of(buscarConta(caso, contaBancariaId).id);
        return conciliar(contasIds);
    }

    public ConciliacaoAutomaticaResultadoDto conciliar(List<UUID> contasIds) {
        if (contasIds.isEmpty()) {
            return new ConciliacaoAutomaticaResultadoDto(0, 0, 0);
        }

        List<MovimentacaoExtrato> pendentes = repository.listarPendentes(contasIds);
        Map<ChaveConciliacao, List<MovimentacaoExtrato>> movimentacoesPorChave = new LinkedHashMap<>();
        for (MovimentacaoExtrato movimentacao : pendentes) {
            movimentacoesPorChave.computeIfAbsent(ChaveConciliacao.de(movimentacao), chave -> new ArrayList<>())
                    .add(movimentacao);
        }

        Map<ChaveConciliacao, List<Lancamento>> lancamentosPorChave = new HashMap<>();
        for (Lancamento lancamento : lancamentoRepository.listarDisponiveisParaConciliacao(contasIds)) {
            lancamentosPorChave.computeIfAbsent(ChaveConciliacao.de(lancamento), chave -> new ArrayList<>())
                    .add(lancamento);
        }

        Instant agora = Instant.now();
        int conciliadas = 0;
        int ambiguas = 0;

        for (Map.Entry<ChaveConciliacao, List<MovimentacaoExtrato>> entrada : movimentacoesPorChave.entrySet()) {
            List<MovimentacaoExtrato> movimentacoes = entrada.getValue();
            List<Lancamento> lancamentos = lancamentosPorChave.getOrDefault(entrada.getKey(), List.of());
            if (lancamentos.isEmpty()) {
                continue;
            }
            if (movimentacoes.size() != lancamentos.size()) {
                ambiguas += movimentacoes.size();
                continue;
            }

            for (int i = 0; i < movimentacoes.size(); i++) {
                MovimentacaoExtrato movimentacao = movimentacoes.get(i);
                movimentacao.lancamento = lancamentos.get(i);
                movimentacao.situacao = SituacaoMovimentacao.CONCILIADA;
                movimentacao.formaConciliacao = FormaConciliacao.AUTOMATICA;
                movimentacao.conciliadaEm = agora;
            }
            conciliadas += movimentacoes.size();
        }

        return new ConciliacaoAutomaticaResultadoDto(conciliadas, pendentes.size() - conciliadas, ambiguas);
    }

    private Map<ChaveConciliacao, Long> contarCandidatos(List<MovimentacaoExtrato> movimentacoes) {
        List<MovimentacaoExtrato> pendentes = movimentacoes.stream()
                .filter(movimentacao -> movimentacao.situacao == SituacaoMovimentacao.PENDENTE)
                .toList();
        if (pendentes.isEmpty()) {
            return Map.of();
        }

        List<UUID> contasIds = pendentes.stream().map(movimentacao -> movimentacao.contaBancaria.id).distinct().toList();
        List<LocalDate> datas = pendentes.stream().map(movimentacao -> movimentacao.dataMovimentacao).distinct().toList();
        List<BigDecimal> valores = pendentes.stream().map(movimentacao -> movimentacao.valor).distinct().toList();

        Map<ChaveConciliacao, Long> candidatos = new HashMap<>();
        for (Object[] linha : lancamentoRepository.contarDisponiveisPorChave(contasIds, datas, valores)) {
            candidatos.put(new ChaveConciliacao((UUID) linha[0], (TipoCategoria) linha[1], (BigDecimal) linha[2],
                    (LocalDate) linha[3]), (Long) linha[4]);
        }
        return candidatos;
    }

    private void validarPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        if (dataInicio != null && dataFim != null && dataInicio.isAfter(dataFim)) {
            throw new BusinessRuleException("A data inicial não pode ser posterior à data final.");
        }
    }

    private Caso buscarCaso(UUID casoId) {
        return casoRepository.buscarPorId(casoId, usuarioLogado())
                .orElseThrow(() -> new ResourceNotFoundException("Caso não encontrado."));
    }

    private Caso buscarCasoEditavel(UUID casoId) {
        Caso caso = buscarCaso(casoId);
        if (caso.isEncerrado()) {
            throw new BusinessRuleException("Reabra o caso para conciliar o extrato.");
        }
        return caso;
    }

    private ContaBancaria buscarConta(Caso caso, UUID contaBancariaId) {
        return contaBancariaRepository.buscarPorId(contaBancariaId, caso.id)
                .orElseThrow(() -> new BusinessRuleException("Conta bancária não encontrada."));
    }

    private UUID usuarioLogado() {
        return UUID.fromString(jwt.getSubject());
    }
}
