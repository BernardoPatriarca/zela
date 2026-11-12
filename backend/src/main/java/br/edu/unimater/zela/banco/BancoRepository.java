package br.edu.unimater.zela.banco;

import br.edu.unimater.zela.shared.dto.Pagina;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class BancoRepository implements PanacheRepositoryBase<Banco, UUID> {

    public Pagina<Banco> listar(UUID usuarioId, String busca, int pagina, int tamanho) {
        if (busca == null || busca.isBlank()) {
            return Pagina.de(find("(usuarioId is null or usuarioId = ?1) and excluidoEm is null order by nome",
                    usuarioId), pagina, tamanho);
        }

        String termo = "%" + busca.trim().toLowerCase() + "%";
        return Pagina.de(find("(usuarioId is null or usuarioId = ?1) and excluidoEm is null and (lower(nome) like ?2 or codigo like ?2) order by nome",
                usuarioId, termo), pagina, tamanho);
    }

    public List<Banco> listarOpcoes(UUID usuarioId) {
        return list("(usuarioId is null or usuarioId = ?1) and excluidoEm is null order by nome", usuarioId);
    }

    public Optional<Banco> buscarPorId(UUID id, UUID usuarioId) {
        return find("id = ?1 and (usuarioId is null or usuarioId = ?2) and excluidoEm is null", id, usuarioId)
                .firstResultOptional();
    }

    public Optional<Banco> buscarPorCodigo(String codigo, UUID usuarioId) {
        return find("codigo = ?1 and (usuarioId is null or usuarioId = ?2) and excluidoEm is null", codigo, usuarioId)
                .firstResultOptional();
    }
}
