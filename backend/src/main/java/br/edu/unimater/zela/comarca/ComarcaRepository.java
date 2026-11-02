package br.edu.unimater.zela.comarca;

import br.edu.unimater.zela.shared.dto.Pagina;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class ComarcaRepository implements PanacheRepositoryBase<Comarca, UUID> {

    public Pagina<Comarca> listar(UUID usuarioId, String busca, int pagina, int tamanho) {
        if (busca == null || busca.isBlank()) {
            return Pagina.de(find("(usuarioId is null or usuarioId = ?1) and excluidoEm is null order by nome",
                    usuarioId), pagina, tamanho);
        }

        return Pagina.de(find("(usuarioId is null or usuarioId = ?1) and excluidoEm is null and lower(nome) like ?2 order by nome",
                usuarioId, "%" + busca.trim().toLowerCase() + "%"), pagina, tamanho);
    }

    public List<Comarca> listarOpcoes(UUID usuarioId) {
        return list("(usuarioId is null or usuarioId = ?1) and excluidoEm is null order by nome", usuarioId);
    }

    public Optional<Comarca> buscarPorId(UUID id, UUID usuarioId) {
        return find("id = ?1 and (usuarioId is null or usuarioId = ?2) and excluidoEm is null", id, usuarioId)
                .firstResultOptional();
    }

    public Optional<Comarca> buscarPorNome(String nome, String uf, UUID usuarioId) {
        return find("lower(nome) = ?1 and uf = ?2 and (usuarioId is null or usuarioId = ?3) and excluidoEm is null",
                nome.trim().toLowerCase(), uf, usuarioId).firstResultOptional();
    }
}
