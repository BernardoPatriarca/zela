package br.edu.unimater.zela.vara;

import br.edu.unimater.zela.shared.dto.Pagina;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class VaraRepository implements PanacheRepositoryBase<Vara, UUID> {

    public Pagina<Vara> listar(UUID usuarioId, UUID comarcaId, String busca, int pagina, int tamanho) {
        StringBuilder filtro = new StringBuilder(" where (v.usuarioId is null or v.usuarioId = :usuarioId) and v.excluidoEm is null");
        Parameters parametros = Parameters.with("usuarioId", usuarioId);

        if (comarcaId != null) {
            filtro.append(" and c.id = :comarcaId");
            parametros.and("comarcaId", comarcaId);
        }
        if (busca != null && !busca.isBlank()) {
            filtro.append(" and lower(v.nome) like :busca");
            parametros.and("busca", "%" + busca.trim().toLowerCase() + "%");
        }

        List<Vara> varas = find("from Vara v join fetch v.comarca c" + filtro + " order by c.nome, v.nome", parametros)
                .page(pagina, tamanho)
                .list();
        long total = count("from Vara v join v.comarca c" + filtro, parametros);
        return new Pagina<>(varas, total, pagina, tamanho);
    }

    public List<Vara> listarOpcoes(UUID usuarioId, UUID comarcaId) {
        if (comarcaId == null) {
            return list("(usuarioId is null or usuarioId = ?1) and excluidoEm is null order by nome", usuarioId);
        }
        return list("comarca.id = ?1 and (usuarioId is null or usuarioId = ?2) and excluidoEm is null order by nome",
                comarcaId, usuarioId);
    }

    public Optional<Vara> buscarPorId(UUID id, UUID usuarioId) {
        return find("""
                from Vara v join fetch v.comarca
                where v.id = ?1 and (v.usuarioId is null or v.usuarioId = ?2) and v.excluidoEm is null
                """, id, usuarioId).firstResultOptional();
    }

    public Optional<Vara> buscarPorNome(String nome, UUID comarcaId, UUID usuarioId) {
        return find("lower(nome) = ?1 and comarca.id = ?2 and (usuarioId is null or usuarioId = ?3) and excluidoEm is null",
                nome.trim().toLowerCase(), comarcaId, usuarioId).firstResultOptional();
    }

    public boolean existePorComarca(UUID comarcaId) {
        return count("comarca.id = ?1 and excluidoEm is null", comarcaId) > 0;
    }
}
