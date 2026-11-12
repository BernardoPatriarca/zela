package br.edu.unimater.zela.importacaoextrato;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class MovimentacaoExtratoRepository implements PanacheRepositoryBase<MovimentacaoExtrato, UUID> {

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
