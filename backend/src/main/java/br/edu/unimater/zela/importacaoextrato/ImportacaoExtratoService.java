package br.edu.unimater.zela.importacaoextrato;

import br.edu.unimater.zela.caso.Caso;
import br.edu.unimater.zela.caso.CasoRepository;
import br.edu.unimater.zela.categoria.TipoCategoria;
import br.edu.unimater.zela.conciliacao.ConciliacaoService;
import br.edu.unimater.zela.contabancaria.ContaBancaria;
import br.edu.unimater.zela.contabancaria.ContaBancariaRepository;
import br.edu.unimater.zela.importacaoextrato.dto.ImportacaoExtratoExclusaoDto;
import br.edu.unimater.zela.importacaoextrato.dto.ImportacaoExtratoResponseDto;
import br.edu.unimater.zela.importacaoextrato.dto.ImportacaoExtratoResultadoDto;
import br.edu.unimater.zela.shared.dto.Pagina;
import br.edu.unimater.zela.shared.exception.BusinessRuleException;
import br.edu.unimater.zela.shared.exception.ConflictException;
import br.edu.unimater.zela.shared.exception.ResourceNotFoundException;
import br.edu.unimater.zela.shared.util.Texto;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@ApplicationScoped
public class ImportacaoExtratoService {

    private static final int TAMANHO_MAXIMO_BYTES = 4 * 1024 * 1024;

    @Inject
    ImportacaoExtratoRepository repository;

    @Inject
    MovimentacaoExtratoRepository movimentacaoRepository;

    @Inject
    ConciliacaoService conciliacaoService;

    @Inject
    CasoRepository casoRepository;

    @Inject
    ContaBancariaRepository contaBancariaRepository;

    @Inject
    JsonWebToken jwt;

    public Pagina<ImportacaoExtratoResponseDto> listar(UUID casoId, UUID contaBancariaId, LocalDate dataInicio,
                                                       LocalDate dataFim, int pagina, int tamanho) {
        Caso caso = buscarCaso(casoId);
        if (dataInicio != null && dataFim != null && dataInicio.isAfter(dataFim)) {
            throw new BusinessRuleException("A data inicial não pode ser posterior à data final.");
        }
        return repository.listar(caso.id, contaBancariaId, dataInicio, dataFim, pagina, tamanho)
                .map(ImportacaoExtratoResponseDto::from);
    }

    @Transactional
    public ImportacaoExtratoResultadoDto importar(UUID casoId, UUID contaBancariaId, String nomeArquivo,
                                                  byte[] conteudo) {
        Caso caso = buscarCasoEditavel(casoId);
        ContaBancaria conta = buscarConta(caso, contaBancariaId);
        validarArquivo(nomeArquivo, conteudo);

        String hashArquivo = sha256(conteudo);
        if (repository.existePorHashArquivo(conta.id, hashArquivo)) {
            throw new ConflictException("Este arquivo já foi importado nesta conta.");
        }

        OfxExtrato extrato = OfxParser.ler(conteudo);

        List<String> avisos = new ArrayList<>();
        if (!contaCompativel(extrato.contaId(), conta.numero)) {
            avisos.add("A conta informada no arquivo é diferente da conta bancária selecionada. "
                    + "Confira se escolheu a conta correta.");
        }
        if (extrato.saldoFinal() == null) {
            avisos.add("Não foi possível ler o saldo final do extrato.");
        }

        Set<String> fitidsExistentes = new HashSet<>(movimentacaoRepository.listarFitids(conta.id));
        Set<String> hashesExistentes = new HashSet<>(movimentacaoRepository.listarHashesSemFitid(conta.id));
        Set<String> fitidsDoArquivo = new HashSet<>();
        Map<String, Integer> ocorrencias = new HashMap<>();
        List<MovimentacaoExtrato> novas = new ArrayList<>();
        int duplicadas = 0;
        int ignoradas = 0;

        for (OfxTransacao transacao : extrato.transacoes()) {
            if (transacao.valor().signum() == 0) {
                ignoradas++;
                continue;
            }

            String chave = conta.id + "|" + transacao.data() + "|" + transacao.valor().toPlainString() + "|"
                    + normalizar(transacao.descricao());
            int ocorrencia = ocorrencias.merge(chave, 1, Integer::sum);
            String hash = sha256((chave + "|" + ocorrencia).getBytes(StandardCharsets.UTF_8));

            boolean duplicada;
            if (transacao.fitid() != null) {
                duplicada = fitidsExistentes.contains(transacao.fitid()) || !fitidsDoArquivo.add(transacao.fitid());
            } else {
                duplicada = hashesExistentes.contains(hash);
            }
            if (duplicada) {
                duplicadas++;
                continue;
            }

            MovimentacaoExtrato movimentacao = new MovimentacaoExtrato();
            movimentacao.contaBancaria = conta;
            movimentacao.dataMovimentacao = transacao.data();
            movimentacao.valor = transacao.valor().abs();
            movimentacao.tipo = transacao.valor().signum() > 0 ? TipoCategoria.RECEITA : TipoCategoria.DESPESA;
            movimentacao.tipoOfx = transacao.tipo();
            movimentacao.fitid = transacao.fitid();
            movimentacao.hash = hash;
            movimentacao.descricao = transacao.descricao();
            movimentacao.numeroDocumento = transacao.numeroDocumento();
            movimentacao.situacao = SituacaoMovimentacao.PENDENTE;
            novas.add(movimentacao);
        }

        ImportacaoExtrato importacao = new ImportacaoExtrato();
        importacao.contaBancaria = conta;
        importacao.nomeArquivo = nomeArquivo.trim();
        importacao.tamanhoBytes = conteudo.length;
        importacao.hashArquivo = hashArquivo;
        importacao.bancoOfx = extrato.bancoId();
        importacao.agenciaOfx = extrato.agenciaId();
        importacao.contaOfx = extrato.contaId();
        importacao.dataInicio = extrato.dataInicio();
        importacao.dataFim = extrato.dataFim();
        importacao.saldoFinal = extrato.saldoFinal();
        importacao.dataSaldoFinal = extrato.dataSaldoFinal();
        importacao.total = extrato.transacoes().size();
        importacao.importadas = novas.size();
        importacao.duplicadas = duplicadas;
        importacao.ignoradas = ignoradas;
        repository.persist(importacao);

        for (MovimentacaoExtrato movimentacao : novas) {
            movimentacao.importacao = importacao;
            movimentacaoRepository.persist(movimentacao);
        }

        repository.flush();
        int conciliadas = conciliacaoService.conciliar(List.of(conta.id)).conciliadas();
        return new ImportacaoExtratoResultadoDto(ImportacaoExtratoResponseDto.from(importacao), conciliadas, avisos);
    }

    @Transactional
    public void excluir(UUID casoId, UUID id, ImportacaoExtratoExclusaoDto dto) {
        ImportacaoExtrato importacao = buscar(buscarCasoEditavel(casoId), id);
        if (movimentacaoRepository.existeVinculadaPorImportacao(id)) {
            throw new BusinessRuleException(
                    "Esta importação possui movimentações vinculadas a lançamentos e não pode ser excluída.");
        }

        Instant agora = Instant.now();
        for (MovimentacaoExtrato movimentacao : movimentacaoRepository.listarPorImportacao(id)) {
            movimentacao.excluidoEm = agora;
        }
        importacao.justificativaExclusao = dto.justificativa().trim();
        importacao.excluidoEm = agora;
    }

    private Caso buscarCaso(UUID casoId) {
        return casoRepository.buscarPorId(casoId, usuarioLogado())
                .orElseThrow(() -> new ResourceNotFoundException("Caso não encontrado."));
    }

    private Caso buscarCasoEditavel(UUID casoId) {
        Caso caso = buscarCaso(casoId);
        if (caso.isEncerrado()) {
            throw new BusinessRuleException("Reabra o caso para alterar os extratos importados.");
        }
        return caso;
    }

    private ImportacaoExtrato buscar(Caso caso, UUID id) {
        return repository.buscarPorId(id, caso.id)
                .orElseThrow(() -> new ResourceNotFoundException("Importação não encontrada."));
    }

    private ContaBancaria buscarConta(Caso caso, UUID contaBancariaId) {
        return contaBancariaRepository.buscarPorId(contaBancariaId, caso.id)
                .orElseThrow(() -> new BusinessRuleException("Conta bancária não encontrada."));
    }

    private void validarArquivo(String nomeArquivo, byte[] conteudo) {
        if (nomeArquivo == null || !nomeArquivo.trim().toLowerCase().endsWith(".ofx")) {
            throw new BusinessRuleException("O arquivo deve ter a extensão .ofx.");
        }
        if (conteudo.length == 0) {
            throw new BusinessRuleException("O arquivo está vazio.");
        }
        if (conteudo.length > TAMANHO_MAXIMO_BYTES) {
            throw new BusinessRuleException("O arquivo deve ter no máximo 4 MB.");
        }
    }

    private boolean contaCompativel(String contaOfx, String numeroConta) {
        String doArquivo = semZerosAEsquerda(Texto.apenasDigitos(contaOfx));
        String selecionada = semZerosAEsquerda(Texto.apenasDigitos(numeroConta));
        if (doArquivo == null || selecionada == null) {
            return true;
        }
        return doArquivo.endsWith(selecionada) || selecionada.endsWith(doArquivo);
    }

    private String semZerosAEsquerda(String digitos) {
        if (digitos == null) {
            return null;
        }
        String resultado = digitos.replaceFirst("^0+", "");
        return resultado.isEmpty() ? null : resultado;
    }

    private String normalizar(String descricao) {
        return descricao.trim().toUpperCase().replaceAll("\\s+", " ");
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
