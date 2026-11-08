package br.edu.unimater.zela.categoria;

import br.edu.unimater.zela.shared.dto.Pagina;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class CategoriaRepository implements PanacheRepositoryBase<Categoria, UUID> {

    public Pagina<Categoria> listar(UUID usuarioId, TipoCategoria tipo, String busca, int pagina, int tamanho) {
        StringBuilder filtro = new StringBuilder("(usuarioId is null or usuarioId = :usuarioId) and excluidoEm is null");
        Parameters parametros = Parameters.with("usuarioId", usuarioId);

        if (tipo != null) {
            filtro.append(" and tipo = :tipo");
            parametros.and("tipo", tipo);
        }
        if (busca != null && !busca.isBlank()) {
            filtro.append(" and lower(nome) like :busca");
            parametros.and("busca", "%" + busca.trim().toLowerCase() + "%");
        }

        return Pagina.de(find(filtro + " order by tipo desc, nome", parametros), pagina, tamanho);
    }

    public List<Categoria> listarOpcoes(UUID usuarioId, TipoCategoria tipo) {
        if (tipo == null) {
            return list("(usuarioId is null or usuarioId = ?1) and excluidoEm is null order by nome", usuarioId);
        }
        return list("tipo = ?1 and (usuarioId is null or usuarioId = ?2) and excluidoEm is null order by nome",
                tipo, usuarioId);
    }

    public Optional<Categoria> buscarPorId(UUID id, UUID usuarioId) {
        return find("id = ?1 and (usuarioId is null or usuarioId = ?2) and excluidoEm is null", id, usuarioId)
                .firstResultOptional();
    }

    public Optional<Categoria> buscarPorNome(String nome, TipoCategoria tipo, UUID usuarioId) {
        return find("lower(nome) = ?1 and tipo = ?2 and (usuarioId is null or usuarioId = ?3) and excluidoEm is null",
                nome.trim().toLowerCase(), tipo, usuarioId).firstResultOptional();
    }
}
