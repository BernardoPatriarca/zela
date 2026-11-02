package br.edu.unimater.zela.caso;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.envers.Audited;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Audited
@Table(name = "periodo_prestacao", schema = "principal")
public class PeriodoPrestacao {

    @Id
    @GeneratedValue
    public UUID id;

    @Column(name = "caso_id", nullable = false, updatable = false)
    public UUID casoId;

    @Column(name = "data_inicio", nullable = false)
    public LocalDate dataInicio;

    @Column(name = "data_fim", nullable = false)
    public LocalDate dataFim;

    @Column(name = "prazo_entrega", nullable = false)
    public LocalDate prazoEntrega;

    @Column(name = "entregue_em")
    public LocalDate entregueEm;

    @CreationTimestamp
    @Column(name = "criado_em", nullable = false, updatable = false)
    public Instant criadoEm;

    @UpdateTimestamp
    @Column(name = "atualizado_em", nullable = false)
    public Instant atualizadoEm;

    @Column(name = "excluido_em")
    public Instant excluidoEm;

    public boolean isAtivo() {
        return excluidoEm == null;
    }

    public void definirDatas(LocalDate inicio, int periodicidadeMeses, int prazoEntregaDias) {
        dataInicio = inicio;
        dataFim = inicio.plusMonths(periodicidadeMeses).minusDays(1);
        prazoEntrega = dataFim.plusDays(prazoEntregaDias);
    }
}
