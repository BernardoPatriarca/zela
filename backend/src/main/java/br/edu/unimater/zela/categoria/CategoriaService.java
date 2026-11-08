package br.edu.unimater.zela.categoria;

import br.edu.unimater.zela.categoria.dto.CategoriaCreateDto;
import br.edu.unimater.zela.categoria.dto.CategoriaResponseDto;
import br.edu.unimater.zela.categoria.dto.CategoriaUpdateDto;
import br.edu.unimater.zela.lancamento.LancamentoRepository;
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
public class CategoriaService {

    @Inject
    CategoriaRepository repository;

    @Inject
    LancamentoRepository lancamentoRepository;

    @Inject
    UsuarioService usuarioService;

    @Inject
    JsonWebToken jwt;

    public Pagina<CategoriaResponseDto> listar(TipoCategoria tipo, String busca, int pagina, int tamanho) {
        return repository.listar(usuarioLogado(), tipo, busca, pagina, tamanho).map(CategoriaResponseDto::from);
    }

    public List<OpcaoDto> listarOpcoes(TipoCategoria tipo) {
        return repository.listarOpcoes(usuarioLogado(), tipo).stream()
                .map(categoria -> new OpcaoDto(categoria.id, categoria.nome))
                .toList();
    }

    public CategoriaResponseDto buscarPorId(UUID id) {
        return CategoriaResponseDto.from(buscar(id));
    }

    @Transactional
    public CategoriaResponseDto criar(CategoriaCreateDto dto) {
        UUID usuarioId = usuarioService.garantirUsuarioLogado();
        validarNomeDisponivel(dto.nome(), dto.tipo(), usuarioId, null);

        Categoria categoria = new Categoria();
        categoria.usuarioId = usuarioId;
        categoria.nome = dto.nome().trim();
        categoria.tipo = dto.tipo();

        repository.persistAndFlush(categoria);
        return CategoriaResponseDto.from(categoria);
    }

    @Transactional
    public CategoriaResponseDto atualizar(UUID id, CategoriaUpdateDto dto) {
        Categoria categoria = buscarEditavel(id);
        validarNomeDisponivel(dto.nome(), categoria.tipo, categoria.usuarioId, id);

        categoria.nome = dto.nome().trim();

        repository.flush();
        return CategoriaResponseDto.from(categoria);
    }

    @Transactional
    public void excluir(UUID id) {
        Categoria categoria = buscarEditavel(id);
        if (lancamentoRepository.existePorCategoria(id)) {
            throw new BusinessRuleException("Esta categoria está vinculada a lançamentos e não pode ser excluída.");
        }
        categoria.excluidoEm = Instant.now();
    }

    private Categoria buscar(UUID id) {
        return repository.buscarPorId(id, usuarioLogado())
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada."));
    }

    private Categoria buscarEditavel(UUID id) {
        Categoria categoria = buscar(id);
        if (categoria.isSistema()) {
            throw new BusinessRuleException("Categorias do sistema não podem ser alteradas.");
        }
        return categoria;
    }

    private void validarNomeDisponivel(String nome, TipoCategoria tipo, UUID usuarioId, UUID idAtual) {
        repository.buscarPorNome(nome, tipo, usuarioId)
                .filter(outra -> !outra.id.equals(idAtual))
                .ifPresent(outra -> {
                    throw new ConflictException("Categoria já cadastrada.");
                });
    }

    private UUID usuarioLogado() {
        return UUID.fromString(jwt.getSubject());
    }
}
