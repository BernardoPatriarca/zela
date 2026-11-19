package br.edu.unimater.zela.prestacaocontas;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class SaldoPeriodoRepository implements PanacheRepositoryBase<SaldoPeriodo, UUID> {

    public List<SaldoPeriodo> listarPorPeriodo(UUID periodoId) {
        return list("periodoId = ?1 and excluidoEm is null", periodoId);
    }
}
