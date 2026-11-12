package br.edu.unimater.zela.banco;

import br.edu.unimater.zela.banco.dto.BancoCreateDto;
import br.edu.unimater.zela.banco.dto.BancoResponseDto;
import br.edu.unimater.zela.banco.dto.BancoUpdateDto;
import br.edu.unimater.zela.contabancaria.ContaBancariaRepository;
import br.edu.unimater.zela.shared.dto.OpcaoDto;
import br.edu.unimater.zela.shared.dto.Pagina;
import br.edu.unimater.zela.shared.exception.BusinessRuleException;
import br.edu.unimater.zela.shared.exception.ConflictException;
import br.edu.unimater.zela.shared.exception.ResourceNotFoundException;
import br.edu.unimater.zela.usuario.UsuarioService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class BancoService {

    @Inject
    BancoRepository repository;

    @Inject
    ContaBancariaRepository contaBancariaRepository;

    @Inject
    UsuarioService usuarioService;

    @Inject
    JsonWebToken jwt;

    public Pagina<BancoResponseDto> listar(String busca, int pagina, int tamanho) {
        return repository.listar(usuarioLogado(), busca, pagina, tamanho).map(BancoResponseDto::from);
    }

    public List<OpcaoDto> listarOpcoes() {
        return repository.listarOpcoes(usuarioLogado()).stream()
                .map(banco -> new OpcaoDto(banco.id, banco.codigo + " - " + banco.nome))
                .toList();
    }

    public BancoResponseDto buscarPorId(UUID id) {
        return BancoResponseDto.from(buscar(id));
    }

    @Transactional
    public BancoResponseDto criar(BancoCreateDto dto) {
        UUID usuarioId = usuarioService.garantirUsuarioLogado();
        validarCodigoDisponivel(dto.codigo(), usuarioId, null);

        Banco banco = new Banco();
        banco.usuarioId = usuarioId;
        banco.codigo = dto.codigo();
        banco.nome = dto.nome().trim();

        repository.persistAndFlush(banco);
        return BancoResponseDto.from(banco);
    }

    @Transactional
    public BancoResponseDto atualizar(UUID id, BancoUpdateDto dto) {
        Banco banco = buscarEditavel(id);
        validarCodigoDisponivel(dto.codigo(), banco.usuarioId, id);

        banco.codigo = dto.codigo();
        banco.nome = dto.nome().trim();

        repository.flush();
        return BancoResponseDto.from(banco);
    }

    @Transactional
    public void excluir(UUID id) {
        Banco banco = buscarEditavel(id);
        if (contaBancariaRepository.existePorBanco(id)) {
            throw new BusinessRuleException("Este banco está vinculado a contas bancárias e não pode ser excluído.");
        }
        banco.excluidoEm = Instant.now();
    }

    private Banco buscar(UUID id) {
        return repository.buscarPorId(id, usuarioLogado())
                .orElseThrow(() -> new ResourceNotFoundException("Banco não encontrado."));
    }

    private Banco buscarEditavel(UUID id) {
        Banco banco = buscar(id);
        if (banco.isSistema()) {
            throw new BusinessRuleException("Bancos do sistema não podem ser alterados.");
        }
        return banco;
    }

    private void validarCodigoDisponivel(String codigo, UUID usuarioId, UUID idAtual) {
        repository.buscarPorCodigo(codigo, usuarioId)
                .filter(outro -> !outro.id.equals(idAtual))
                .ifPresent(outro -> {
                    throw new ConflictException("Banco já cadastrado com este código.");
                });
    }

    private UUID usuarioLogado() {
        return UUID.fromString(jwt.getSubject());
    }
}
