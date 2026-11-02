package br.edu.unimater.zela.caso;

import br.edu.unimater.zela.caso.dto.CasoCreateDto;
import br.edu.unimater.zela.caso.dto.CasoEncerramentoDto;
import br.edu.unimater.zela.caso.dto.CasoResponseDto;
import br.edu.unimater.zela.caso.dto.CasoUpdateDto;
import br.edu.unimater.zela.contabancaria.ContaBancariaRepository;
import br.edu.unimater.zela.pessoa.Pessoa;
import br.edu.unimater.zela.pessoa.PessoaRepository;
import br.edu.unimater.zela.shared.dto.OpcaoDto;
import br.edu.unimater.zela.shared.dto.Pagina;
import br.edu.unimater.zela.shared.exception.BusinessRuleException;
import br.edu.unimater.zela.shared.exception.ConflictException;
import br.edu.unimater.zela.shared.exception.ResourceNotFoundException;
import br.edu.unimater.zela.shared.util.NumeroCnj;
import br.edu.unimater.zela.shared.util.Texto;
import br.edu.unimater.zela.usuario.UsuarioService;
import br.edu.unimater.zela.vara.Vara;
import br.edu.unimater.zela.vara.VaraRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class CasoService {

    @Inject
    CasoRepository repository;

    @Inject
    PeriodoPrestacaoRepository periodoRepository;

    @Inject
    VaraRepository varaRepository;

    @Inject
    PessoaRepository pessoaRepository;

    @Inject
    ContaBancariaRepository contaBancariaRepository;

    @Inject
    UsuarioService usuarioService;

    @Inject
    JsonWebToken jwt;

    public Pagina<CasoResponseDto> listar(TipoCaso tipo, UUID comarcaId, SituacaoCaso situacao, String busca,
                                          int pagina, int tamanho) {
        Pagina<Caso> casos = repository.listar(usuarioLogado(), tipo, comarcaId, situacao, busca, pagina, tamanho);
        Map<UUID, PeriodoPrestacao> periodos = periodoRepository.buscarAbertos(
                casos.itens().stream().map(caso -> caso.id).toList());

        return casos.map(caso -> CasoResponseDto.from(caso, periodos.get(caso.id)));
    }

    public List<OpcaoDto> listarOpcoes() {
        return repository.listarOpcoes(usuarioLogado()).stream()
                .map(caso -> new OpcaoDto(caso.id,
                        caso.pessoaProtegida.nome + " · " + NumeroCnj.formatar(caso.numeroProcesso)))
                .toList();
    }

    public CasoResponseDto buscarPorId(UUID id) {
        return responder(buscar(id));
    }

    @Transactional
    public CasoResponseDto criar(CasoCreateDto dto) {
        UUID usuarioId = usuarioService.garantirUsuarioLogado();
        String numeroProcesso = validarNumeroProcesso(dto.numeroProcesso(), usuarioId, null);
        validarPessoasDiferentes(dto.pessoaProtegidaId(), dto.responsavelId());

        Caso caso = new Caso();
        caso.usuarioId = usuarioId;
        caso.tipo = dto.tipo();
        caso.numeroProcesso = numeroProcesso;
        caso.vara = buscarVara(dto.varaId(), usuarioId);
        caso.pessoaProtegida = buscarPessoa(dto.pessoaProtegidaId(), usuarioId, "Pessoa protegida não encontrada.");
        caso.responsavel = buscarPessoa(dto.responsavelId(), usuarioId, "Responsável não encontrado.");
        caso.periodicidadeMeses = dto.periodicidadeMeses();
        caso.prazoEntregaDias = dto.prazoEntregaDias();
        repository.persist(caso);

        PeriodoPrestacao periodo = new PeriodoPrestacao();
        periodo.casoId = caso.id;
        periodo.definirDatas(dto.inicioPeriodo(), caso.periodicidadeMeses, caso.prazoEntregaDias);
        periodoRepository.persist(periodo);

        repository.flush();
        return CasoResponseDto.from(caso, periodo);
    }

    @Transactional
    public CasoResponseDto atualizar(UUID id, CasoUpdateDto dto) {
        Caso caso = buscar(id);
        if (caso.isEncerrado()) {
            throw new BusinessRuleException("Reabra o caso para alterá-lo.");
        }
        String numeroProcesso = validarNumeroProcesso(dto.numeroProcesso(), caso.usuarioId, id);
        validarPessoasDiferentes(dto.pessoaProtegidaId(), dto.responsavelId());

        caso.tipo = dto.tipo();
        caso.numeroProcesso = numeroProcesso;
        caso.vara = buscarVara(dto.varaId(), caso.usuarioId);
        caso.pessoaProtegida = buscarPessoa(dto.pessoaProtegidaId(), caso.usuarioId, "Pessoa protegida não encontrada.");
        caso.responsavel = buscarPessoa(dto.responsavelId(), caso.usuarioId, "Responsável não encontrado.");
        caso.periodicidadeMeses = dto.periodicidadeMeses();
        caso.prazoEntregaDias = dto.prazoEntregaDias();

        PeriodoPrestacao periodo = periodoRepository.buscarAberto(caso.id).orElse(null);
        if (periodo != null) {
            validarInicioPeriodo(caso.id, dto.inicioPeriodo());
            periodo.definirDatas(dto.inicioPeriodo(), caso.periodicidadeMeses, caso.prazoEntregaDias);
        }

        repository.flush();
        return CasoResponseDto.from(caso, periodo);
    }

    @Transactional
    public CasoResponseDto encerrar(UUID id, CasoEncerramentoDto dto) {
        Caso caso = buscar(id);
        if (caso.isEncerrado()) {
            throw new BusinessRuleException("O caso já está encerrado.");
        }
        caso.encerradoEm = dto.data();

        repository.flush();
        return responder(caso);
    }

    @Transactional
    public CasoResponseDto reabrir(UUID id) {
        Caso caso = buscar(id);
        if (!caso.isEncerrado()) {
            throw new BusinessRuleException("O caso não está encerrado.");
        }
        caso.encerradoEm = null;

        repository.flush();
        return responder(caso);
    }

    @Transactional
    public void excluir(UUID id) {
        Caso caso = buscar(id);
        if (contaBancariaRepository.existePorCaso(id)) {
            throw new BusinessRuleException("Exclua as contas bancárias do caso antes de excluí-lo.");
        }
        caso.excluidoEm = Instant.now();
    }

    private Caso buscar(UUID id) {
        return repository.buscarPorId(id, usuarioLogado())
                .orElseThrow(() -> new ResourceNotFoundException("Caso não encontrado."));
    }

    private CasoResponseDto responder(Caso caso) {
        return CasoResponseDto.from(caso, periodoRepository.buscarAberto(caso.id).orElse(null));
    }

    private Vara buscarVara(UUID varaId, UUID usuarioId) {
        return varaRepository.buscarPorId(varaId, usuarioId)
                .orElseThrow(() -> new BusinessRuleException("Vara não encontrada."));
    }

    private Pessoa buscarPessoa(UUID pessoaId, UUID usuarioId, String mensagem) {
        return pessoaRepository.buscarPorId(pessoaId, usuarioId)
                .orElseThrow(() -> new BusinessRuleException(mensagem));
    }

    private String validarNumeroProcesso(String valor, UUID usuarioId, UUID idAtual) {
        String numeroProcesso = Texto.apenasDigitos(valor);
        if (!NumeroCnj.valido(numeroProcesso)) {
            throw new BusinessRuleException("Número do processo inválido.");
        }
        repository.buscarPorNumeroProcesso(numeroProcesso, usuarioId)
                .filter(outro -> !outro.id.equals(idAtual))
                .ifPresent(outro -> {
                    throw new ConflictException("Já existe um caso com este número de processo.");
                });
        return numeroProcesso;
    }

    private void validarPessoasDiferentes(UUID pessoaProtegidaId, UUID responsavelId) {
        if (pessoaProtegidaId.equals(responsavelId)) {
            throw new BusinessRuleException("A pessoa protegida e o responsável devem ser pessoas diferentes.");
        }
    }

    private void validarInicioPeriodo(UUID casoId, LocalDate inicio) {
        periodoRepository.buscarUltimoEntregue(casoId)
                .filter(ultimo -> !inicio.isAfter(ultimo.dataFim))
                .ifPresent(ultimo -> {
                    throw new BusinessRuleException("O período atual deve começar depois do último período entregue.");
                });
    }

    private UUID usuarioLogado() {
        return UUID.fromString(jwt.getSubject());
    }
}
