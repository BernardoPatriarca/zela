package br.edu.unimater.zela.prestacaocontas;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class JustificativaPendenciaRepository implements PanacheRepositoryBase<JustificativaPendencia, UUID> {

    public List<JustificativaPendencia> listarPorPeriodo(UUID periodoId) {
        return list("periodoId = ?1 and excluidoEm is null", periodoId);
    }

    public Optional<JustificativaPendencia> buscar(UUID periodoId, TipoPendencia tipo, String referencia) {
        return find("periodoId = ?1 and tipo = ?2 and referencia = ?3 and excluidoEm is null",
                periodoId, tipo, referencia).firstResultOptional();
    }
}
