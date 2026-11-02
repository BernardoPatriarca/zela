package br.edu.unimater.zela.pessoa;

import br.edu.unimater.zela.shared.dto.Pagina;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class PessoaRepository implements PanacheRepositoryBase<Pessoa, UUID> {

    public Pagina<Pessoa> listar(UUID usuarioId, String busca, int pagina, int tamanho) {
        if (busca == null || busca.isBlank()) {
            return Pagina.de(find("usuarioId = ?1 and excluidoEm is null order by nome", usuarioId), pagina, tamanho);
        }

        return Pagina.de(find("usuarioId = ?1 and excluidoEm is null and lower(nome) like ?2 order by nome",
                usuarioId, "%" + busca.trim().toLowerCase() + "%"), pagina, tamanho);
    }

    public List<Pessoa> listarOpcoes(UUID usuarioId) {
        return list("usuarioId = ?1 and excluidoEm is null order by nome", usuarioId);
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