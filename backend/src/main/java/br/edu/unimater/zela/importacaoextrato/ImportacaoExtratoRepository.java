package br.edu.unimater.zela.importacaoextrato;

import br.edu.unimater.zela.shared.dto.Pagina;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class ImportacaoExtratoRepository implements PanacheRepositoryBase<ImportacaoExtrato, UUID> {

    private static final String CONSULTA_COMPLETA = """
            from ImportacaoExtrato i
            join fetch i.contaBancaria c
            """;

    private static final String CONSULTA_CONTAGEM = """
            from ImportacaoExtrato i
            join i.contaBancaria c
            """;

    public Pagina<ImportacaoExtrato> listar(UUID casoId, UUID contaBancariaId, LocalDate dataInicio,
                                            LocalDate dataFim, int pagina, int tamanho) {
        StringBuilder query = new StringBuilder("where c.casoId = :casoId and i.excluidoEm is null");
        Parameters parametros = Parameters.with("casoId", casoId);

        if (contaBancariaId != null) {
            query.append(" and c.id = :contaBancariaId");
            parametros.and("contaBancariaId", contaBancariaId);
        }
        if (dataInicio != null) {
            query.append(" and i.dataFim >= :dataInicio");
            parametros.and("dataInicio", dataInicio);
        }
        if (dataFim != null) {
            query.append(" and i.dataInicio <= :dataFim");
            parametros.and("dataFim", dataFim);
        }

        List<ImportacaoExtrato> importacoes = find(CONSULTA_COMPLETA + query + " order by i.criadoEm desc",
                parametros)
                .page(pagina, tamanho)
                .list();
        long total = count(CONSULTA_CONTAGEM + query, parametros);
        return new Pagina<>(importacoes, total, pagina, tamanho);
    }

    public Optional<ImportacaoExtrato> buscarPorId(UUID id, UUID casoId) {
        return find(CONSULTA_COMPLETA + "where i.id = ?1 and c.casoId = ?2 and i.excluidoEm is null", id, casoId)
                .firstResultOptional();
    }

    public List<ImportacaoExtrato> listarQueTocamPeriodo(List<UUID> contasIds, LocalDate dataInicio,
                                                         LocalDate dataFim) {
        return list("""
                from ImportacaoExtrato i
                join fetch i.contaBancaria c
                where c.id in ?1 and i.dataFim >= ?2 and i.dataInicio <= ?3 and i.excluidoEm is null
                order by i.dataInicio
                """, contasIds, dataInicio, dataFim);
    }

    public List<ImportacaoExtrato> listarComSaldoNoPeriodo(List<UUID> contasIds, LocalDate dataInicio,
                                                           LocalDate dataFim) {
        return list("""
                from ImportacaoExtrato i
                join fetch i.contaBancaria c
                where c.id in ?1 and i.saldoFinal is not null and i.dataSaldoFinal between ?2 and ?3
                and i.excluidoEm is null
                order by i.dataSaldoFinal desc, i.criadoEm desc
                """, contasIds, dataInicio, dataFim);
    }

    public boolean existePorHashArquivo(UUID contaBancariaId, String hashArquivo) {
        return count("contaBancaria.id = ?1 and hashArquivo = ?2 and excluidoEm is null", contaBancariaId,
                hashArquivo) > 0;
    }

    public boolean existePorContaBancaria(UUID contaBancariaId) {
        return count("contaBancaria.id = ?1 and excluidoEm is null", contaBancariaId) > 0;
    }
}
