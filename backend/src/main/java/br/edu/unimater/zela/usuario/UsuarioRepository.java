package br.edu.unimater.zela.usuario;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class UsuarioRepository implements PanacheRepositoryBase<Usuario, UUID> {

    public List<Usuario> listar() {
        return list("excluidoEm is null order by nome");
    }

    public Optional<Usuario> buscarPorId(UUID id) {
        return find("id = ?1 and excluidoEm is null", id).firstResultOptional();
    }

    public Optional<Usuario> buscarPorEmail(String email) {
        return find("lower(email) = ?1 and excluidoEm is null", email.trim().toLowerCase()).firstResultOptional();
    }

    public boolean excluir(UUID id) {
        return buscarPorId(id).map(usuario -> {
            usuario.excluidoEm = Instant.now();
            return true;
        }).orElse(false);
    }
}