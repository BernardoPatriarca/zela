package br.edu.unimater.zela.prestacaocontas;

import br.edu.unimater.zela.caso.Caso;
import br.edu.unimater.zela.caso.CasoRepository;
import br.edu.unimater.zela.caso.PeriodoPrestacao;
import br.edu.unimater.zela.caso.PeriodoPrestacaoRepository;
import br.edu.unimater.zela.caso.SituacaoPeriodo;
import br.edu.unimater.zela.contabancaria.ContaBancaria;
import br.edu.unimater.zela.prestacaocontas.dto.EntregaRequestDto;
import br.edu.unimater.zela.prestacaocontas.dto.JustificativaRequestDto;
import br.edu.unimater.zela.prestacaocontas.dto.PendenciaDto;
import br.edu.unimater.zela.prestacaocontas.dto.PeriodoAtualizacaoDto;
import br.edu.unimater.zela.prestacaocontas.dto.PrestacaoContasDetalheDto;
import br.edu.unimater.zela.prestacaocontas.dto.PrestacaoContasResumoDto;
import br.edu.unimater.zela.prestacaocontas.dto.RelatorioResponseDto;
import br.edu.unimater.zela.prestacaocontas.dto.SaldoInicialRequestDto;
import br.edu.unimater.zela.prestacaocontas.dto.SaldoPeriodoDto;
import br.edu.unimater.zela.shared.dto.Pagina;
import br.edu.unimater.zela.shared.exception.BusinessRuleException;
import br.edu.unimater.zela.shared.exception.ResourceNotFoundException;
import br.edu.unimater.zela.shared.util.Texto;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class PrestacaoContasService {

    private static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");

    @Inject
    PeriodoPrestacaoRepository periodoRepository;

    @Inject
    CasoRepository casoRepository;

    @Inject
    SaldoPeriodoRepository saldoRepository;

    @Inject
    JustificativaPendenciaRepository justificativaRepository;

    @Inject
    RelatorioPrestacaoRepository relatorioRepository;

    @Inject
    DadosPeriodoService dadosPeriodoService;

    @Inject
    PendenciaService pendenciaService;

    @Inject
    SaldoService saldoService;

    @Inject
    JsonWebToken jwt;

    public Pagina<PrestacaoContasResumoDto> listar(UUID casoId, SituacaoPeriodo situacao, int pagina, int tamanho) {
        if (casoId != null) {
            buscarCaso(casoId);
        }
        LocalDate hoje = LocalDate.now(FUSO);
        return periodoRepository.listarComCaso(usuarioLogado(), casoId, situacao, pagina, tamanho)
                .map(linha -> PrestacaoContasResumoDto.from((PeriodoPrestacao) linha[0], (Caso) linha[1], hoje));
    }

    public PrestacaoContasDetalheDto buscarPorId(UUID id) {
        PeriodoPrestacao periodo = buscarPeriodo(id);
        return detalhar(buscarCaso(periodo.casoId), periodo);
    }

    @Transactional
    public PrestacaoContasDetalheDto atualizar(UUID id, PeriodoAtualizacaoDto dto) {
        PeriodoPrestacao periodo = buscarPeriodoParaAtualizar(id);
        Caso caso = buscarCasoEditavel(periodo.casoId);
        validarNaoEntregue(periodo);
        if (dto.prazoCurador() != null && dto.prazoCurador().isAfter(periodo.prazoEntrega)) {
            throw new BusinessRuleException("O prazo do curador não pode ser posterior ao prazo legal.");
        }

        periodo.prazoCurador = dto.prazoCurador();
        periodo.notasExplicativas = Texto.trimOuNulo(dto.notasExplicativas());

        periodoRepository.flush();
        return detalhar(caso, periodo);
    }

    @Transactional
    public void informarSaldosIniciais(UUID id, List<SaldoInicialRequestDto> saldos) {
        PeriodoPrestacao periodo = buscarPeriodoParaAtualizar(id);
        Caso caso = buscarCasoEditavel(periodo.casoId);
        validarNaoEntregue(periodo);

        Map<UUID, ContaBancaria> contas = new HashMap<>();
        for (ContaBancaria conta : dadosPeriodoService.contasDoCaso(caso)) {
            contas.put(conta.id, conta);
        }
        Map<UUID, SaldoPeriodo> existentes = new HashMap<>();
        for (SaldoPeriodo saldo : saldoRepository.listarPorPeriodo(periodo.id)) {
            existentes.put(saldo.contaBancariaId, saldo);
        }

        for (SaldoInicialRequestDto dto : saldos) {
            if (!contas.containsKey(dto.contaBancariaId())) {
                throw new BusinessRuleException("Conta bancária não encontrada.");
            }
            SaldoPeriodo saldo = existentes.get(dto.contaBancariaId());
            if (saldo == null) {
                saldo = new SaldoPeriodo();
                saldo.periodoId = periodo.id;
                saldo.contaBancariaId = dto.contaBancariaId();
                saldo.saldoInicial = dto.saldoInicial();
                saldoRepository.persist(saldo);
                existentes.put(saldo.contaBancariaId, saldo);
            } else {
                saldo.saldoInicial = dto.saldoInicial();
            }
        }

        saldoRepository.flush();
    }

    @Transactional
    public void salvarJustificativa(UUID id, JustificativaRequestDto dto) {
        PeriodoPrestacao periodo = buscarPeriodoParaAtualizar(id);
        buscarCasoEditavel(periodo.casoId);
        validarNaoEntregue(periodo);

        String texto = Texto.trimOuNulo(dto.texto());
        Optional<JustificativaPendencia> existente = justificativaRepository.buscar(periodo.id, dto.tipo(),
                dto.referencia());
        if (texto == null) {
            existente.ifPresent(justificativa -> justificativa.excluidoEm = Instant.now());
        } else if (existente.isPresent()) {
            existente.get().texto = texto;
        } else {
            JustificativaPendencia justificativa = new JustificativaPendencia();
            justificativa.periodoId = periodo.id;
            justificativa.tipo = dto.tipo();
            justificativa.referencia = dto.referencia();
            justificativa.texto = texto;
            justificativaRepository.persist(justificativa);
        }

        justificativaRepository.flush();
    }

    @Transactional
    public PrestacaoContasDetalheDto receberDoCurador(UUID id) {
        PeriodoPrestacao periodo = buscarPeriodoParaAtualizar(id);
        Caso caso = buscarCasoEditavel(periodo.casoId);
        if (periodo.situacao != SituacaoPeriodo.AGUARDANDO_CURADOR) {
            throw new BusinessRuleException("Esta prestação de contas não está aguardando o curador.");
        }
        periodo.situacao = SituacaoPeriodo.EM_REVISAO;

        periodoRepository.flush();
        return detalhar(caso, periodo);
    }

    @Transactional
    public PrestacaoContasDetalheDto voltarParaRevisao(UUID id) {
        PeriodoPrestacao periodo = buscarPeriodoParaAtualizar(id);
        Caso caso = buscarCasoEditavel(periodo.casoId);
        if (periodo.situacao != SituacaoPeriodo.PRONTO) {
            throw new BusinessRuleException("Só é possível voltar para a revisão uma prestação de contas pronta.");
        }
        periodo.situacao = SituacaoPeriodo.EM_REVISAO;

        periodoRepository.flush();
        return detalhar(caso, periodo);
    }

    @Transactional
    public PrestacaoContasDetalheDto entregar(UUID id, EntregaRequestDto dto) {
        PeriodoPrestacao periodo = buscarPeriodoParaAtualizar(id);
        Caso caso = buscarCasoEditavel(periodo.casoId);
        if (periodo.situacao != SituacaoPeriodo.PRONTO) {
            throw new BusinessRuleException("Só é possível marcar como entregue uma prestação de contas pronta.");
        }
        RelatorioPrestacao ultimo = relatorioRepository.buscarUltimo(periodo.id)
                .orElseThrow(() -> new BusinessRuleException("Gere o relatório antes de marcar como entregue."));
        if (dto.dataEntrega().isBefore(LocalDate.ofInstant(ultimo.geradoEm, FUSO))) {
            throw new BusinessRuleException("A data de entrega não pode ser anterior à geração do último relatório.");
        }
        if (dto.dataEntrega().isBefore(periodo.dataFim)) {
            throw new BusinessRuleException("A data de entrega não pode ser anterior ao fim do período.");
        }

        DadosPeriodo dados = dadosPeriodoService.carregar(caso, periodo);
        saldoService.exigirSaldosIniciais(dados.saldosIniciais(), dados.contas());

        Map<UUID, SaldoPeriodo> linhas = new HashMap<>();
        for (SaldoPeriodo saldo : saldoRepository.listarPorPeriodo(periodo.id)) {
            linhas.put(saldo.contaBancariaId, saldo);
        }
        Map<UUID, BigDecimal> saldosFinais = new HashMap<>();
        BigDecimal total = BigDecimal.ZERO;
        for (ContaBancaria conta : dados.contas()) {
            BigDecimal saldoFinal = saldoService.saldoEm(dados.saldosIniciais().get(conta.id),
                    dados.lancamentosPorConta().getOrDefault(conta.id, List.of()), periodo.dataFim);
            saldosFinais.put(conta.id, saldoFinal);
            total = total.add(saldoFinal);
        }
        if (total.compareTo(ultimo.saldoFinalTotal) != 0) {
            throw new BusinessRuleException(
                    "Os lançamentos mudaram depois do último relatório. Volte para a revisão e gere um novo relatório.");
        }

        for (ContaBancaria conta : dados.contas()) {
            linhas.get(conta.id).saldoFinal = saldosFinais.get(conta.id);
        }
        periodo.entregueEm = dto.dataEntrega();
        periodo.situacao = SituacaoPeriodo.ENTREGUE;
        periodoRepository.flush();

        PeriodoPrestacao proximo = new PeriodoPrestacao();
        proximo.casoId = caso.id;
        proximo.definirDatas(periodo.dataFim.plusDays(1), caso.periodicidadeMeses, caso.prazoEntregaDias);
        proximo.prazoEntrega = proximo.dataFim.plusDays(ChronoUnit.DAYS.between(periodo.dataFim, periodo.prazoEntrega));
        if (periodo.prazoCurador != null) {
            proximo.prazoCurador = proximo.dataFim.plusDays(ChronoUnit.DAYS.between(periodo.dataFim,
                    periodo.prazoCurador));
        }
        periodoRepository.persist(proximo);
        periodoRepository.flush();

        for (ContaBancaria conta : dados.contas()) {
            SaldoPeriodo saldo = new SaldoPeriodo();
            saldo.periodoId = proximo.id;
            saldo.contaBancariaId = conta.id;
            saldo.saldoInicial = saldosFinais.get(conta.id);
            saldoRepository.persist(saldo);
        }
        saldoRepository.flush();

        return detalhar(caso, periodo);
    }

    private PrestacaoContasDetalheDto detalhar(Caso caso, PeriodoPrestacao periodo) {
        DadosPeriodo dados = dadosPeriodoService.carregar(caso, periodo);
        List<SaldoPeriodoDto> saldos = saldosDoPeriodo(caso, periodo, dados.contas());
        List<PendenciaDto> pendencias = pendenciaService.listar(caso, periodo, dados);
        List<RelatorioResponseDto> relatorios = relatorioRepository.listarPorPeriodo(periodo.id).stream()
                .map(RelatorioResponseDto::from)
                .toList();
        return PrestacaoContasDetalheDto.from(periodo, caso, LocalDate.now(FUSO), saldos, pendencias, relatorios);
    }

    private List<SaldoPeriodoDto> saldosDoPeriodo(Caso caso, PeriodoPrestacao periodo, List<ContaBancaria> contas) {
        Map<UUID, SaldoPeriodo> linhas = new HashMap<>();
        for (SaldoPeriodo saldo : saldoRepository.listarPorPeriodo(periodo.id)) {
            linhas.put(saldo.contaBancariaId, saldo);
        }
        Map<UUID, BigDecimal> finaisAnteriores = new HashMap<>();
        periodoRepository.buscarEntregueAnterior(caso.id, periodo.dataInicio).ifPresent(anterior -> {
            for (SaldoPeriodo saldo : saldoRepository.listarPorPeriodo(anterior.id)) {
                if (saldo.saldoFinal != null) {
                    finaisAnteriores.put(saldo.contaBancariaId, saldo.saldoFinal);
                }
            }
        });

        return contas.stream().map(conta -> {
            SaldoPeriodo linha = linhas.get(conta.id);
            return new SaldoPeriodoDto(
                    conta.id,
                    conta.banco.nome + " · Ag. " + conta.agencia + " · Conta " + conta.numero,
                    linha == null ? null : linha.saldoInicial,
                    finaisAnteriores.getOrDefault(conta.id, conta.saldoInicial),
                    linha == null ? null : linha.saldoFinal);
        }).toList();
    }

    private void validarNaoEntregue(PeriodoPrestacao periodo) {
        if (periodo.isEntregue()) {
            throw new BusinessRuleException("Esta prestação de contas já foi entregue e não pode mais ser alterada.");
        }
    }

    private PeriodoPrestacao buscarPeriodo(UUID id) {
        return periodoRepository.buscarPorId(id, usuarioLogado())
                .orElseThrow(() -> new ResourceNotFoundException("Prestação de contas não encontrada."));
    }

    private PeriodoPrestacao buscarPeriodoParaAtualizar(UUID id) {
        return periodoRepository.buscarParaAtualizar(id, usuarioLogado())
                .orElseThrow(() -> new ResourceNotFoundException("Prestação de contas não encontrada."));
    }

    private Caso buscarCaso(UUID casoId) {
        return casoRepository.buscarPorId(casoId, usuarioLogado())
                .orElseThrow(() -> new ResourceNotFoundException("Caso não encontrado."));
    }

    private Caso buscarCasoEditavel(UUID casoId) {
        Caso caso = buscarCaso(casoId);
        if (caso.isEncerrado()) {
            throw new BusinessRuleException("Reabra o caso para alterar a prestação de contas.");
        }
        return caso;
    }

    private UUID usuarioLogado() {
        return UUID.fromString(jwt.getSubject());
    }
}
