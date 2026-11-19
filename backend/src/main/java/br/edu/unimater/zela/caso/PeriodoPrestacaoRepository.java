package br.edu.unimater.zela.caso;

import br.edu.unimater.zela.shared.dto.Pagina;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.LockModeType;
import jakarta.persistence.TypedQuery;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@ApplicationScoped
public class PeriodoPrestacaoRepository implements PanacheRepositoryBase<PeriodoPrestacao, UUID> {

    public Optional<PeriodoPrestacao> buscarPorId(UUID id, UUID usuarioId) {
        return find("""
                from PeriodoPrestacao p
                where p.id = ?1 and p.excluidoEm is null
                and exists (select 1 from Caso c where c.id = p.casoId and c.usuarioId = ?2 and c.excluidoEm is null)
                """, id, usuarioId).firstResultOptional();
    }

    public Optional<PeriodoPrestacao> buscarParaAtualizar(UUID id, UUID usuarioId) {
        return find("""
                from PeriodoPrestacao p
                where p.id = ?1 and p.excluidoEm is null
                and exists (select 1 from Caso c where c.id = p.casoId and c.usuarioId = ?2 and c.excluidoEm is null)
                """, id, usuarioId).withLock(LockModeType.PESSIMISTIC_WRITE).firstResultOptional();
    }

    public Pagina<Object[]> listarComCaso(UUID usuarioId, UUID casoId, SituacaoPeriodo situacao, int pagina,
                                          int tamanho) {
        StringBuilder query = new StringBuilder("""
                where c.id = p.casoId and c.usuarioId = :usuarioId and c.excluidoEm is null
                and p.excluidoEm is null
                """);
        Parameters parametros = Parameters.with("usuarioId", usuarioId);

        if (casoId != null) {
            query.append(" and c.id = :casoId");
            parametros.and("casoId", casoId);
        }
        if (situacao != null) {
            query.append(" and p.situacao = :situacao");
            parametros.and("situacao", situacao);
        }

        TypedQuery<Object[]> consulta = getEntityManager().createQuery("""
                select p, c from PeriodoPrestacao p, Caso c
                join fetch c.pessoaProtegida pp
                """ + query + """
                 order by case when p.entregueEm is null then 0 else 1 end,
                case when p.entregueEm is null then p.prazoEntrega end,
                p.prazoEntrega desc, p.id
                """, Object[].class);
        TypedQuery<Long> contagem = getEntityManager().createQuery(
                "select count(p) from PeriodoPrestacao p, Caso c " + query, Long.class);
        parametros.map().forEach((nome, valor) -> {
            consulta.setParameter(nome, valor);
            contagem.setParameter(nome, valor);
        });

        List<Object[]> itens = consulta.setFirstResult(pagina * tamanho).setMaxResults(tamanho).getResultList();
        return new Pagina<>(itens, contagem.getSingleResult(), pagina, tamanho);
    }

    public Optional<PeriodoPrestacao> buscarEntregueAnterior(UUID casoId, LocalDate dataInicio) {
        return find("casoId = ?1 and entregueEm is not null and excluidoEm is null and dataFim < ?2 "
                + "order by dataFim desc", casoId, dataInicio).firstResultOptional();
    }

    public List<PeriodoPrestacao> listarEntregues(UUID casoId) {
        return list("casoId = ?1 and entregueEm is not null and excluidoEm is null", casoId);
    }

    public boolean existeEntregueNaData(UUID casoId, LocalDate data) {
        return count("casoId = ?1 and entregueEm is not null and excluidoEm is null "
                + "and dataInicio <= ?2 and dataFim >= ?2", casoId, data) > 0;
    }

    public Optional<PeriodoPrestacao> buscarAberto(UUID casoId) {
        return find("casoId = ?1 and entregueEm is null and excluidoEm is null", casoId).firstResultOptional();
    }

    public Map<UUID, PeriodoPrestacao> buscarAbertos(List<UUID> casoIds) {
        if (casoIds.isEmpty()) {
            return Map.of();
        }
        return list("casoId in ?1 and entregueEm is null and excluidoEm is null", casoIds).stream()
                .collect(Collectors.toMap(periodo -> periodo.casoId, Function.identity()));
    }

    public Optional<PeriodoPrestacao> buscarUltimoEntregue(UUID casoId) {
        return find("casoId = ?1 and entregueEm is not null and excluidoEm is null order by dataFim desc", casoId)
                .firstResultOptional();
    }
}
