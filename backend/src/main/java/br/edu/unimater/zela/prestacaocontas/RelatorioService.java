package br.edu.unimater.zela.prestacaocontas;

import br.edu.unimater.zela.caso.Caso;
import br.edu.unimater.zela.caso.CasoRepository;
import br.edu.unimater.zela.caso.PeriodoPrestacao;
import br.edu.unimater.zela.caso.PeriodoPrestacaoRepository;
import br.edu.unimater.zela.caso.SituacaoPeriodo;
import br.edu.unimater.zela.prestacaocontas.dto.ArquivoRelatorioDto;
import br.edu.unimater.zela.prestacaocontas.dto.DemonstrativoDto;
import br.edu.unimater.zela.prestacaocontas.dto.PendenciaDto;
import br.edu.unimater.zela.prestacaocontas.dto.RelatorioResponseDto;
import br.edu.unimater.zela.prestacaocontas.dto.RessalvaDto;
import br.edu.unimater.zela.shared.exception.BusinessRuleException;
import br.edu.unimater.zela.shared.exception.ResourceNotFoundException;
import br.edu.unimater.zela.usuario.Usuario;
import br.edu.unimater.zela.usuario.UsuarioRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class RelatorioService {

    @Inject
    RelatorioPrestacaoRepository repository;

    @Inject
    PeriodoPrestacaoRepository periodoRepository;

    @Inject
    CasoRepository casoRepository;

    @Inject
    UsuarioRepository usuarioRepository;

    @Inject
    DadosPeriodoService dadosPeriodoService;

    @Inject
    PendenciaService pendenciaService;

    @Inject
    DemonstrativoService demonstrativoService;

    @Inject
    RelatorioPdf relatorioPdf;

    @Inject
    ArmazenamentoRelatorios armazenamento;

    @Inject
    JsonWebToken jwt;

    public List<RelatorioResponseDto> listar(UUID periodoId) {
        PeriodoPrestacao periodo = buscarPeriodo(periodoId);
        return repository.listarPorPeriodo(periodo.id).stream().map(RelatorioResponseDto::from).toList();
    }

    @Transactional
    public RelatorioResponseDto gerar(UUID periodoId) {
        UUID usuarioId = usuarioLogado();
        PeriodoPrestacao periodo = periodoRepository.buscarParaAtualizar(periodoId, usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Prestação de contas não encontrada."));
        Caso caso = casoRepository.buscarPorId(periodo.casoId, usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Caso não encontrado."));
        if (caso.isEncerrado()) {
            throw new BusinessRuleException("Reabra o caso para gerar o relatório.");
        }
        if (periodo.situacao == SituacaoPeriodo.ENTREGUE) {
            throw new BusinessRuleException("Esta prestação de contas já foi entregue e não pode gerar novos relatórios.");
        }
        if (periodo.situacao == SituacaoPeriodo.AGUARDANDO_CURADOR) {
            throw new BusinessRuleException("Receba os documentos do curador antes de gerar o relatório.");
        }
        Usuario advogado = usuarioRepository.buscarPorId(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));
        if (advogado.nomeCompleto == null || advogado.oabNumero == null) {
            throw new BusinessRuleException("Informe seu nome completo e sua OAB antes de gerar o relatório.");
        }

        DadosPeriodo dados = dadosPeriodoService.carregar(caso, periodo);
        List<PendenciaDto> pendencias = pendenciaService.listar(caso, periodo, dados);
        List<RessalvaDto> ressalvas = pendencias.stream()
                .map(pendencia -> new RessalvaDto(pendencia.texto(), pendencia.justificativa()))
                .toList();
        DemonstrativoDto demonstrativo = demonstrativoService.montar(caso, periodo, advogado, dados, ressalvas,
                periodo.notasExplicativas);
        byte[] pdf = relatorioPdf.gerar(demonstrativo.documento());

        int versao = repository.proximaVersao(periodo.id);
        RelatorioPrestacao relatorio = new RelatorioPrestacao();
        relatorio.periodoId = periodo.id;
        relatorio.versao = versao;
        relatorio.geradoPor = usuarioId;
        relatorio.chaveObjeto = caso.id + "/" + periodo.id + "/v" + versao + ".pdf";
        relatorio.hashSha256 = sha256(pdf);
        relatorio.tamanhoBytes = pdf.length;
        relatorio.comRessalvas = !pendencias.isEmpty();
        relatorio.quantidadePendencias = pendencias.size();
        relatorio.saldoFinalTotal = demonstrativo.saldoFinalTotal();
        repository.persistAndFlush(relatorio);
        periodo.situacao = SituacaoPeriodo.PRONTO;

        armazenamento.enviar(relatorio.chaveObjeto, pdf);
        return RelatorioResponseDto.from(relatorio);
    }

    public ArquivoRelatorioDto baixar(UUID periodoId, UUID relatorioId) {
        PeriodoPrestacao periodo = buscarPeriodo(periodoId);
        RelatorioPrestacao relatorio = repository.buscarPorId(relatorioId, periodo.id)
                .orElseThrow(() -> new ResourceNotFoundException("Relatório não encontrado."));
        String nome = "prestacao-contas-" + periodo.dataInicio + "-a-" + periodo.dataFim + "-v" + relatorio.versao
                + ".pdf";
        return new ArquivoRelatorioDto(nome, armazenamento.baixar(relatorio.chaveObjeto));
    }

    private PeriodoPrestacao buscarPeriodo(UUID periodoId) {
        return periodoRepository.buscarPorId(periodoId, usuarioLogado())
                .orElseThrow(() -> new ResourceNotFoundException("Prestação de contas não encontrada."));
    }

    private String sha256(byte[] conteudo) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(conteudo));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private UUID usuarioLogado() {
        return UUID.fromString(jwt.getSubject());
    }
}
