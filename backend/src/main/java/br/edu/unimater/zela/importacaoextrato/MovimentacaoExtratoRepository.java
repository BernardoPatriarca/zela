package br.edu.unimater.zela.importacaoextrato;

import br.edu.unimater.zela.shared.dto.Pagina;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.TypedQuery;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class MovimentacaoExtratoRepository implements PanacheRepositoryBase<MovimentacaoExtrato, UUID> {

    private static final String CONSULTA_COMPLETA = """
            from MovimentacaoExtrato m
            join fetch m.contaBancaria c
            left join fetch m.lancamento l
            left join fetch l.categoria ca
            """;

    private static final String CONSULTA_CONTAGEM = """
            from MovimentacaoExtrato m
            join m.contaBancaria c
            """;

    public Pagina<MovimentacaoExtrato> listar(UUID casoId, UUID contaBancariaId, LocalDate dataInicio,
                                              LocalDate dataFim, SituacaoMovimentacao situacao,
                                              int pagina, int tamanho) {
        StringBuilder query = new StringBuilder("where c.casoId = :casoId and m.excluidoEm is null");
        Parameters parametros = Parameters.with("casoId", casoId);

        if (contaBancariaId != null) {
            query.append(" and c.id = :contaBancariaId");
            parametros.and("contaBancariaId", contaBancariaId);
        }
        if (dataInicio != null) {
            query.append(" and m.dataMovimentacao >= :dataInicio");
            parametros.and("dataInicio", dataInicio);
        }
        if (dataFim != null) {
            query.append(" and m.dataMovimentacao <= :dataFim");
            parametros.and("dataFim", dataFim);
        }
        if (situacao != null) {
            query.append(" and m.situacao = :situacao");
            parametros.and("situacao", situacao);
        }

        List<MovimentacaoExtrato> movimentacoes = find(
                CONSULTA_COMPLETA + query + " order by m.dataMovimentacao desc, m.criadoEm desc, m.id", parametros)
                .page(pagina, tamanho)
                .list();
        long total = count(CONSULTA_CONTAGEM + query, parametros);
        return new Pagina<>(movimentacoes, total, pagina, tamanho);
    }

    public Map<SituacaoMovimentacao, Long> contarPorSituacao(UUID casoId, UUID contaBancariaId, LocalDate dataInicio,
                                                             LocalDate dataFim) {
        StringBuilder query = new StringBuilder("where c.casoId = :casoId and m.excluidoEm is null");
        Parameters parametros = Parameters.with("casoId", casoId);

        if (contaBancariaId != null) {
            query.append(" and c.id = :contaBancariaId");
            parametros.and("contaBancariaId", contaBancariaId);
        }
        if (dataInicio != null) {
            query.append(" and m.dataMovimentacao >= :dataInicio");
            parametros.and("dataInicio", dataInicio);
        }
        if (dataFim != null) {
            query.append(" and m.dataMovimentacao <= :dataFim");
            parametros.and("dataFim", dataFim);
        }

        TypedQuery<Object[]> consulta = getEntityManager().createQuery(
                "select m.situacao, count(m) " + CONSULTA_CONTAGEM + query + " group by m.situacao", Object[].class);
        parametros.map().forEach(consulta::setParameter);

        Map<SituacaoMovimentacao, Long> contagens = new EnumMap<>(SituacaoMovimentacao.class);
        for (SituacaoMovimentacao situacao : SituacaoMovimentacao.values()) {
            contagens.put(situacao, 0L);
        }
        for (Object[] linha : consulta.getResultList()) {
            contagens.put((SituacaoMovimentacao) linha[0], (Long) linha[1]);
        }
        return contagens;
    }

    public List<MovimentacaoExtrato> listarPendentes(List<UUID> contasIds) {
        return list("contaBancaria.id in ?1 and situacao = ?2 and excluidoEm is null order by criadoEm, id",
                contasIds, SituacaoMovimentacao.PENDENTE);
    }

    public List<UUID> listarLancamentosConciliados(List<UUID> lancamentosIds) {
        if (lancamentosIds.isEmpty()) {
            return List.of();
        }
        return getEntityManager()
                .createQuery("select m.lancamento.id from MovimentacaoExtrato m "
                        + "where m.lancamento.id in ?1 and m.excluidoEm is null", UUID.class)
                .setParameter(1, lancamentosIds)
                .getResultList();
    }

    public boolean existePorLancamento(UUID lancamentoId) {
        return count("lancamento.id = ?1 and excluidoEm is null", lancamentoId) > 0;
    }

    public List<String> listarFitids(UUID contaBancariaId) {
        return getEntityManager()
                .createQuery("select m.fitid from MovimentacaoExtrato m "
                        + "where m.contaBancaria.id = ?1 and m.fitid is not null and m.excluidoEm is null",
                        String.class)
                .setParameter(1, contaBancariaId)
                .getResultList();
    }

    public List<String> listarHashesSemFitid(UUID contaBancariaId) {
        return getEntityManager()
                .createQuery("select m.hash from MovimentacaoExtrato m "
                        + "where m.contaBancaria.id = ?1 and m.fitid is null and m.excluidoEm is null",
                        String.class)
                .setParameter(1, contaBancariaId)
                .getResultList();
    }

    public List<MovimentacaoExtrato> listarPorImportacao(UUID importacaoId) {
        return list("importacao.id = ?1 and excluidoEm is null", importacaoId);
    }

    public boolean existeVinculadaPorImportacao(UUID importacaoId) {
        return count("importacao.id = ?1 and lancamento is not null and excluidoEm is null", importacaoId) > 0;
    }
}
