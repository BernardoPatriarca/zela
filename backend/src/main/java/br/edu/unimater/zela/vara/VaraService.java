package br.edu.unimater.zela.vara;

import br.edu.unimater.zela.caso.CasoRepository;
import br.edu.unimater.zela.comarca.Comarca;
import br.edu.unimater.zela.comarca.ComarcaRepository;
import br.edu.unimater.zela.shared.dto.OpcaoDto;
import br.edu.unimater.zela.shared.dto.Pagina;
import br.edu.unimater.zela.shared.exception.BusinessRuleException;
import br.edu.unimater.zela.shared.exception.ConflictException;
import br.edu.unimater.zela.shared.exception.ResourceNotFoundException;
import br.edu.unimater.zela.usuario.UsuarioService;
import br.edu.unimater.zela.vara.dto.VaraCreateDto;
import br.edu.unimater.zela.vara.dto.VaraResponseDto;
import br.edu.unimater.zela.vara.dto.VaraUpdateDto;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class VaraService {

    @Inject
    VaraRepository repository;

    @Inject
    ComarcaRepository comarcaRepository;

    @Inject
    CasoRepository casoRepository;

    @Inject
    UsuarioService usuarioService;

    @Inject
    JsonWebToken jwt;

    public Pagina<VaraResponseDto> listar(UUID comarcaId, String busca, int pagina, int tamanho) {
        return repository.listar(usuarioLogado(), comarcaId, busca, pagina, tamanho).map(VaraResponseDto::from);
    }

    public List<OpcaoDto> listarOpcoes(UUID comarcaId) {
        return repository.listarOpcoes(usuarioLogado(), comarcaId).stream()
                .map(vara -> new OpcaoDto(vara.id, vara.nome))
                .toList();
    }

    public VaraResponseDto buscarPorId(UUID id) {
        return VaraResponseDto.from(buscar(id));
    }

    @Transactional
    public VaraResponseDto criar(VaraCreateDto dto) {
        UUID usuarioId = usuarioService.garantirUsuarioLogado();
        Comarca comarca = buscarComarca(dto.comarcaId(), usuarioId);
        validarNomeDisponivel(dto.nome(), comarca.id, usuarioId, null);

        Vara vara = new Vara();
        vara.usuarioId = usuarioId;
        vara.comarca = comarca;
        vara.nome = dto.nome().trim();

        repository.persistAndFlush(vara);
        return VaraResponseDto.from(vara);
    }

    @Transactional
    public VaraResponseDto atualizar(UUID id, VaraUpdateDto dto) {
        Vara vara = buscarEditavel(id);
        Comarca comarca = buscarComarca(dto.comarcaId(), vara.usuarioId);
        validarNomeDisponivel(dto.nome(), comarca.id, vara.usuarioId, id);

        vara.comarca = comarca;
        vara.nome = dto.nome().trim();

        repository.flush();
        return VaraResponseDto.from(vara);
    }

    @Transactional
    public void excluir(UUID id) {
        Vara vara = buscarEditavel(id);
        if (casoRepository.existePorVara(id)) {
            throw new BusinessRuleException("Esta vara está vinculada a um caso e não pode ser excluída.");
        }
        vara.excluidoEm = Instant.now();
    }

    private Vara buscar(UUID id) {
        return repository.buscarPorId(id, usuarioLogado())
                .orElseThrow(() -> new ResourceNotFoundException("Vara não encontrada."));
    }

    private Vara buscarEditavel(UUID id) {
        Vara vara = buscar(id);
        if (vara.isSistema()) {
            throw new BusinessRuleException("Varas do sistema não podem ser alteradas.");
        }
        return vara;
    }

    private Comarca buscarComarca(UUID comarcaId, UUID usuarioId) {
        return comarcaRepository.buscarPorId(comarcaId, usuarioId)
                .orElseThrow(() -> new BusinessRuleException("Comarca não encontrada."));
    }

    private void validarNomeDisponivel(String nome, UUID comarcaId, UUID usuarioId, UUID idAtual) {
        repository.buscarPorNome(nome, comarcaId, usuarioId)
                .filter(outra -> !outra.id.equals(idAtual))
                .ifPresent(outra -> {
                    throw new ConflictException("Vara já cadastrada nesta comarca.");
                });
    }

    private UUID usuarioLogado() {
        return UUID.fromString(jwt.getSubject());
    }
}
