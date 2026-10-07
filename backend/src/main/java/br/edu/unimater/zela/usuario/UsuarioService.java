package br.edu.unimater.zela.usuario;

import br.edu.unimater.zela.shared.exception.ConflictException;
import br.edu.unimater.zela.shared.exception.ResourceNotFoundException;
import br.edu.unimater.zela.usuario.dto.UsuarioCreateDto;
import br.edu.unimater.zela.usuario.dto.UsuarioResponseDto;
import br.edu.unimater.zela.usuario.dto.UsuarioUpdateDto;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

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
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));
        return UsuarioResponseDto.from(usuario);
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
}