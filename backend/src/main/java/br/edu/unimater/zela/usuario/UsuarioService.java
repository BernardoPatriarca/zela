package br.edu.unimater.zela.usuario;

import br.edu.unimater.zela.shared.exception.ConflictException;
import br.edu.unimater.zela.shared.exception.ResourceNotFoundException;
import br.edu.unimater.zela.usuario.dto.UsuarioCreateDto;
import br.edu.unimater.zela.usuario.dto.UsuarioResponseDto;
import br.edu.unimater.zela.usuario.dto.UsuarioUpdateDto;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.JsonObject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class UsuarioService {

    @Inject
    UsuarioRepository repository;

    @Inject
    JsonWebToken jwt;

    public List<UsuarioResponseDto> listar() {
        return repository.listar().stream().map(UsuarioResponseDto::from).toList();
    }

    public UsuarioResponseDto buscarPorId(UUID id) {
        Usuario usuario = repository.buscarPorId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));
        return UsuarioResponseDto.from(usuario);
    }

    @Transactional
    public UsuarioResponseDto me() {
        UUID id = UUID.fromString(jwt.getSubject());
        String email = jwt.getClaim("email");
        JsonObject metadata = jwt.getClaim("user_metadata");
        String nome = metadata == null ? null : metadata.getString("nome", null);

        return repository.buscarPorId(id)
                .map(UsuarioResponseDto::from)
                .orElseGet(() -> provisionar(id, email, nome));
    }

    @Transactional
    public UsuarioResponseDto criar(UsuarioCreateDto dto) {
        String email = dto.email().trim().toLowerCase();

        if (repository.findByIdOptional(dto.id()).isPresent()) {
            throw new ConflictException("Já existe um usuário com esse id.");
        }
        if (repository.buscarPorEmail(email).isPresent()) {
            throw new ConflictException("E-mail já cadastrado.");
        }

        Usuario usuario = new Usuario();
        usuario.id = dto.id();
        usuario.nome = dto.nome().trim();
        usuario.email = email;

        repository.persistAndFlush(usuario);
        return UsuarioResponseDto.from(usuario);
    }

    @Transactional
    public UsuarioResponseDto atualizar(UUID id, UsuarioUpdateDto dto) {
        Usuario usuario = repository.buscarPorId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));
        String email = dto.email().trim().toLowerCase();

        repository.buscarPorEmail(email)
                .filter(outro -> !outro.id.equals(id))
                .ifPresent(outro -> {
                    throw new ConflictException("E-mail já cadastrado.");
                });

        usuario.nome = dto.nome().trim();
        usuario.email = email;

        repository.flush();
        return UsuarioResponseDto.from(usuario);
    }

    @Transactional
    public void excluir(UUID id) {
        if (!repository.excluir(id)) {
            throw new ResourceNotFoundException("Usuário não encontrado.");
        }
    }

    private UsuarioResponseDto provisionar(UUID id, String email, String nome) {
        String emailNormalizado = email.trim().toLowerCase();

        if (repository.buscarPorEmail(emailNormalizado).isPresent()) {
            throw new ConflictException("E-mail já cadastrado.");
        }

        Usuario usuario = new Usuario();
        usuario.id = id;
        usuario.nome = nome == null || nome.isBlank() ? emailNormalizado : nome.trim();
        usuario.email = emailNormalizado;

        repository.persistAndFlush(usuario);
        return UsuarioResponseDto.from(usuario);
    }
}
