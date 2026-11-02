package br.edu.unimater.zela.caso;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@ApplicationScoped
public class PeriodoPrestacaoRepository implements PanacheRepositoryBase<PeriodoPrestacao, UUID> {

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
