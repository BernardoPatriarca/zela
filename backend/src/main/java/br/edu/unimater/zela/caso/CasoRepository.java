package br.edu.unimater.zela.caso;

import br.edu.unimater.zela.shared.dto.Pagina;
import br.edu.unimater.zela.shared.util.Texto;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class CasoRepository implements PanacheRepositoryBase<Caso, UUID> {

    private static final String CONSULTA_COMPLETA = """
            from Caso c
            join fetch c.vara v
            join fetch v.comarca co
            join fetch c.pessoaProtegida p
            join fetch c.responsavel r
            """;

    private static final String CONSULTA_CONTAGEM = """
            from Caso c
            join c.vara v
            join v.comarca co
            join c.pessoaProtegida p
            join c.responsavel r
            """;

    public Pagina<Caso> listar(UUID usuarioId, TipoCaso tipo, UUID comarcaId, SituacaoCaso situacao, String busca,
                               int pagina, int tamanho) {
        StringBuilder query = new StringBuilder("where c.usuarioId = :usuarioId and c.excluidoEm is null");
        Parameters parametros = Parameters.with("usuarioId", usuarioId);

        if (tipo != null) {
            query.append(" and c.tipo = :tipo");
            parametros.and("tipo", tipo);
        }
        if (comarcaId != null) {
            query.append(" and co.id = :comarcaId");
            parametros.and("comarcaId", comarcaId);
        }
        if (situacao == SituacaoCaso.ATIVO) {
            query.append(" and c.encerradoEm is null");
        } else if (situacao == SituacaoCaso.ENCERRADO) {
            query.append(" and c.encerradoEm is not null");
        }
        if (busca != null && !busca.isBlank()) {
            String digitos = Texto.apenasDigitos(busca);
            query.append(" and (lower(p.nome) like :nome or lower(r.nome) like :nome");
            parametros.and("nome", "%" + busca.trim().toLowerCase() + "%");
            if (digitos != null) {
                query.append(" or c.numeroProcesso like :numero");
                parametros.and("numero", "%" + digitos + "%");
            }
            query.append(")");
        }

        List<Caso> casos = find(CONSULTA_COMPLETA + query + " order by p.nome", parametros)
                .page(pagina, tamanho)
                .list();
        long total = count(CONSULTA_CONTAGEM + query, parametros);
        return new Pagina<>(casos, total, pagina, tamanho);
    }

    public List<Caso> listarOpcoes(UUID usuarioId) {
        return list("from Caso c join fetch c.pessoaProtegida p where c.usuarioId = ?1 and c.excluidoEm is null order by p.nome",
                usuarioId);
    }

    public Optional<Caso> buscarPorId(UUID id, UUID usuarioId) {
        return find(CONSULTA_COMPLETA + "where c.id = ?1 and c.usuarioId = ?2 and c.excluidoEm is null", id, usuarioId)
                .firstResultOptional();
    }

    public Optional<Caso> buscarPorNumeroProcesso(String numeroProcesso, UUID usuarioId) {
        return find("numeroProcesso = ?1 and usuarioId = ?2 and excluidoEm is null", numeroProcesso, usuarioId)
                .firstResultOptional();
    }

    public boolean existePorPessoa(UUID pessoaId, UUID usuarioId) {
        return count("(pessoaProtegida.id = ?1 or responsavel.id = ?1) and usuarioId = ?2 and excluidoEm is null",
                pessoaId, usuarioId) > 0;
    }

    public boolean existePorVara(UUID varaId) {
        return count("vara.id = ?1 and excluidoEm is null", varaId) > 0;
    }
}
