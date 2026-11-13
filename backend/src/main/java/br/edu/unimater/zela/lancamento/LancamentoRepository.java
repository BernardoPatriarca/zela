package br.edu.unimater.zela.lancamento;

import br.edu.unimater.zela.categoria.TipoCategoria;
import br.edu.unimater.zela.shared.dto.Pagina;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class LancamentoRepository implements PanacheRepositoryBase<Lancamento, UUID> {

    private static final String CONSULTA_COMPLETA = """
            from Lancamento l
            join fetch l.contaBancaria c
            join fetch l.categoria ca
            """;

    private static final String CONSULTA_CONTAGEM = """
            from Lancamento l
            join l.contaBancaria c
            join l.categoria ca
            """;

    public Pagina<Lancamento> listar(UUID casoId, LocalDate dataInicio, LocalDate dataFim, UUID contaBancariaId,
                                     TipoCategoria tipo, UUID categoriaId, SituacaoLancamento situacao,
                                     int pagina, int tamanho) {
        StringBuilder query = new StringBuilder("where c.casoId = :casoId and l.excluidoEm is null");
        Parameters parametros = Parameters.with("casoId", casoId);

        if (dataInicio != null) {
            query.append(" and l.dataLancamento >= :dataInicio");
            parametros.and("dataInicio", dataInicio);
        }
        if (dataFim != null) {
            query.append(" and l.dataLancamento <= :dataFim");
            parametros.and("dataFim", dataFim);
        }
        if (contaBancariaId != null) {
            query.append(" and c.id = :contaBancariaId");
            parametros.and("contaBancariaId", contaBancariaId);
        }
        if (tipo != null) {
            query.append(" and l.tipo = :tipo");
            parametros.and("tipo", tipo);
        }
        if (categoriaId != null) {
            query.append(" and ca.id = :categoriaId");
            parametros.and("categoriaId", categoriaId);
        }
        if (situacao == SituacaoLancamento.CONCILIADO) {
            query.append(" and exists (select 1 from MovimentacaoExtrato m where m.lancamento = l"
                    + " and m.excluidoEm is null)");
        } else if (situacao == SituacaoLancamento.PENDENTE) {
            query.append(" and not exists (select 1 from MovimentacaoExtrato m where m.lancamento = l"
                    + " and m.excluidoEm is null)");
        }

        List<Lancamento> lancamentos = find(CONSULTA_COMPLETA + query + " order by l.dataLancamento desc, l.criadoEm desc",
                parametros)
                .page(pagina, tamanho)
                .list();
        long total = count(CONSULTA_CONTAGEM + query, parametros);
        return new Pagina<>(lancamentos, total, pagina, tamanho);
    }

    public Optional<Lancamento> buscarPorId(UUID id, UUID casoId) {
        return find(CONSULTA_COMPLETA + "where l.id = ?1 and c.casoId = ?2 and l.excluidoEm is null", id, casoId)
                .firstResultOptional();
    }

    public List<Lancamento> listarDisponiveisParaConciliacao(List<UUID> contasIds) {
        return list("""
                from Lancamento l
                where l.contaBancaria.id in ?1 and l.excluidoEm is null
                and not exists (select 1 from MovimentacaoExtrato m where m.lancamento = l and m.excluidoEm is null)
                order by l.criadoEm, l.id
                """, contasIds);
    }

    public List<Object[]> contarDisponiveisPorChave(List<UUID> contasIds, List<LocalDate> datas,
                                                    List<BigDecimal> valores) {
        return getEntityManager()
                .createQuery("""
                        select l.contaBancaria.id, l.tipo, l.valor, l.dataLancamento, count(l)
                        from Lancamento l
                        where l.contaBancaria.id in ?1 and l.dataLancamento in ?2 and l.valor in ?3
                        and l.excluidoEm is null
                        and not exists (select 1 from MovimentacaoExtrato m where m.lancamento = l and m.excluidoEm is null)
                        group by l.contaBancaria.id, l.tipo, l.valor, l.dataLancamento
                        """, Object[].class)
                .setParameter(1, contasIds)
                .setParameter(2, datas)
                .setParameter(3, valores)
                .getResultList();
    }

    public boolean existePorCategoria(UUID categoriaId) {
        return count("categoria.id = ?1 and excluidoEm is null", categoriaId) > 0;
    }

    public boolean existePorContaBancaria(UUID contaBancariaId) {
        return count("contaBancaria.id = ?1 and excluidoEm is null", contaBancariaId) > 0;
    }
}
