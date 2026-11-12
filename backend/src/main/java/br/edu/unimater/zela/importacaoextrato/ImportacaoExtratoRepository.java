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

    public boolean existePorHashArquivo(UUID contaBancariaId, String hashArquivo) {
        return count("contaBancaria.id = ?1 and hashArquivo = ?2 and excluidoEm is null", contaBancariaId,
                hashArquivo) > 0;
    }

    public boolean existePorContaBancaria(UUID contaBancariaId) {
        return count("contaBancaria.id = ?1 and excluidoEm is null", contaBancariaId) > 0;
    }
}
