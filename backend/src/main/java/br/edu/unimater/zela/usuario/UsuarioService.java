package br.edu.unimater.zela.usuario;

import br.edu.unimater.zela.usuario.dto.UsuarioCreateDto;
import br.edu.unimater.zela.usuario.dto.UsuarioResponseDto;
import br.edu.unimater.zela.usuario.dto.UsuarioUpdateDto;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class UsuarioService {

    @Inject
    UsuarioRepository repository;

    public List<UsuarioResponseDto> listar() {
        return repository.listar().stream().map(UsuarioResponseDto::from).toList();
    }

    public UsuarioResponseDto buscarPorId(UUID id) {
        Usuario usuario = repository.buscarPorId(id)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado."));

        return UsuarioResponseDto.from(usuario);
    }

    @Transactional
    public UsuarioResponseDto criar(UsuarioCreateDto dto) {
        if (dto == null || dto.id() == null) {
            throw new BadRequestException("O id é obrigatório.");
        }
        validarDados(dto.nome(), dto.email());

        String email = dto.email().trim().toLowerCase();

        if (repository.findByIdOptional(dto.id()).isPresent()) {
            throw new WebApplicationException("Já existe um usuário com esse id.", Response.Status.CONFLICT);
        }
        if (repository.buscarPorEmail(email).isPresent()) {
            throw new WebApplicationException("E-mail já cadastrado.", Response.Status.CONFLICT);
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
        if (dto == null) {
            throw new BadRequestException("Dados do usuário não informados.");
        }
        validarDados(dto.nome(), dto.email());

        Usuario usuario = repository.buscarPorId(id)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado."));
        String email = dto.email().trim().toLowerCase();

        repository.buscarPorEmail(email)
                .filter(outro -> !outro.id.equals(id))
                .ifPresent(outro -> {
                    throw new WebApplicationException("E-mail já cadastrado.", Response.Status.CONFLICT);
                });

        usuario.nome = dto.nome().trim();
        usuario.email = email;

        repository.flush();
        return UsuarioResponseDto.from(usuario);
    }

    @Transactional
    public void excluir(UUID id) {
        if (!repository.excluir(id)) {
            throw new NotFoundException("Usuário não encontrado.");
        }
    }

    private static void validarDados(String nome, String email) {
        if (nome == null || nome.isBlank()) {
            throw new BadRequestException("O nome é obrigatório.");
        }
        if (nome.trim().length() > 150) {
            throw new BadRequestException("O nome deve ter no máximo 150 caracteres.");
        }
        if (email == null || email.isBlank()) {
            throw new BadRequestException("O e-mail é obrigatório.");
        }
        if (email.trim().length() > 255) {
            throw new BadRequestException("O e-mail deve ter no máximo 255 caracteres.");
        }
        if (!email.contains("@")) {
            throw new BadRequestException("E-mail inválido.");
        }
    }
}