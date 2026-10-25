package br.edu.unimater.zela.pessoa;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class PessoaRepository implements PanacheRepositoryBase<Pessoa, UUID> {

    public List<Pessoa> listar(UUID usuarioId, String busca) {
        if (busca == null || busca.isBlank()) {
            return list("usuarioId = ?1 and excluidoEm is null order by nome", usuarioId);
        }

        return list("usuarioId = ?1 and excluidoEm is null and lower(nome) like ?2 order by nome",
                usuarioId, "%" + busca.trim().toLowerCase() + "%");
    }

    public Optional<Pessoa> buscarPorId(UUID id, UUID usuarioId) {
        return find("id = ?1 and usuarioId = ?2 and excluidoEm is null", id, usuarioId).firstResultOptional();
    }

    public Optional<Pessoa> buscarPorCpf(String cpf, UUID usuarioId) {
        return find("cpf = ?1 and usuarioId = ?2 and excluidoEm is null", cpf, usuarioId).firstResultOptional();
    }

    public boolean excluir(UUID id, UUID usuarioId) {
        return buscarPorId(id, usuarioId).map(pessoa -> {
            pessoa.excluidoEm = Instant.now();
            return true;
        }).orElse(false);
    }
}