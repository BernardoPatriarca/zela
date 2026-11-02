package br.edu.unimater.zela.comarca;

import br.edu.unimater.zela.comarca.dto.ComarcaCreateDto;
import br.edu.unimater.zela.comarca.dto.ComarcaResponseDto;
import br.edu.unimater.zela.comarca.dto.ComarcaUpdateDto;
import br.edu.unimater.zela.shared.dto.OpcaoDto;
import br.edu.unimater.zela.shared.dto.Pagina;
import br.edu.unimater.zela.shared.exception.BusinessRuleException;
import br.edu.unimater.zela.shared.exception.ConflictException;
import br.edu.unimater.zela.shared.exception.ResourceNotFoundException;
import br.edu.unimater.zela.usuario.UsuarioService;
import br.edu.unimater.zela.vara.VaraRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class ComarcaService {

    @Inject
    ComarcaRepository repository;

    @Inject
    VaraRepository varaRepository;

    @Inject
    UsuarioService usuarioService;

    @Inject
    JsonWebToken jwt;

    public Pagina<ComarcaResponseDto> listar(String busca, int pagina, int tamanho) {
        return repository.listar(usuarioLogado(), busca, pagina, tamanho).map(ComarcaResponseDto::from);
    }

    public List<OpcaoDto> listarOpcoes() {
        return repository.listarOpcoes(usuarioLogado()).stream()
                .map(comarca -> new OpcaoDto(comarca.id, comarca.nome))
                .toList();
    }

    public ComarcaResponseDto buscarPorId(UUID id) {
        return ComarcaResponseDto.from(buscar(id));
    }

    @Transactional
    public ComarcaResponseDto criar(ComarcaCreateDto dto) {
        UUID usuarioId = usuarioService.garantirUsuarioLogado();
        String uf = dto.uf().toUpperCase();
        validarNomeDisponivel(dto.nome(), uf, usuarioId, null);

        Comarca comarca = new Comarca();
        comarca.usuarioId = usuarioId;
        comarca.nome = dto.nome().trim();
        comarca.uf = uf;

        repository.persistAndFlush(comarca);
        return ComarcaResponseDto.from(comarca);
    }

    @Transactional
    public ComarcaResponseDto atualizar(UUID id, ComarcaUpdateDto dto) {
        Comarca comarca = buscarEditavel(id);
        String uf = dto.uf().toUpperCase();
        validarNomeDisponivel(dto.nome(), uf, comarca.usuarioId, id);

        comarca.nome = dto.nome().trim();
        comarca.uf = uf;

        repository.flush();
        return ComarcaResponseDto.from(comarca);
    }

    @Transactional
    public void excluir(UUID id) {
        Comarca comarca = buscarEditavel(id);
        if (varaRepository.existePorComarca(id)) {
            throw new BusinessRuleException("Exclua as varas desta comarca antes de excluí-la.");
        }
        comarca.excluidoEm = Instant.now();
    }

    private Comarca buscar(UUID id) {
        return repository.buscarPorId(id, usuarioLogado())
                .orElseThrow(() -> new ResourceNotFoundException("Comarca não encontrada."));
    }

    private Comarca buscarEditavel(UUID id) {
        Comarca comarca = buscar(id);
        if (comarca.isSistema()) {
            throw new BusinessRuleException("Comarcas do sistema não podem ser alteradas.");
        }
        return comarca;
    }

    private void validarNomeDisponivel(String nome, String uf, UUID usuarioId, UUID idAtual) {
        repository.buscarPorNome(nome, uf, usuarioId)
                .filter(outra -> !outra.id.equals(idAtual))
                .ifPresent(outra -> {
                    throw new ConflictException("Comarca já cadastrada.");
                });
    }

    private UUID usuarioLogado() {
        return UUID.fromString(jwt.getSubject());
    }
}
