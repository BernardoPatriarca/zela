package br.edu.unimater.zela.prestacaocontas;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class RelatorioPrestacaoRepository implements PanacheRepositoryBase<RelatorioPrestacao, UUID> {

    public List<RelatorioPrestacao> listarPorPeriodo(UUID periodoId) {
        return list("periodoId = ?1 order by versao desc", periodoId);
    }

    public Optional<RelatorioPrestacao> buscarUltimo(UUID periodoId) {
        return find("periodoId = ?1 order by versao desc", periodoId).firstResultOptional();
    }

    public Optional<RelatorioPrestacao> buscarPorId(UUID id, UUID periodoId) {
        return find("id = ?1 and periodoId = ?2", id, periodoId).firstResultOptional();
    }

    public int proximaVersao(UUID periodoId) {
        Integer maxima = getEntityManager()
                .createQuery("select max(r.versao) from RelatorioPrestacao r where r.periodoId = ?1", Integer.class)
                .setParameter(1, periodoId)
                .getSingleResult();
        return maxima == null ? 1 : maxima + 1;
    }
}
