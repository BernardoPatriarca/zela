package br.edu.unimater.zela.prestacaocontas;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.envers.Audited;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Audited
@Table(name = "saldo_periodo", schema = "principal")
public class SaldoPeriodo {

    @Id
    @GeneratedValue
    public UUID id;

    @Column(name = "periodo_id", nullable = false, updatable = false)
    public UUID periodoId;

    @Column(name = "conta_bancaria_id", nullable = false, updatable = false)
    public UUID contaBancariaId;

    @Column(name = "saldo_inicial", nullable = false, precision = 15, scale = 2)
    public BigDecimal saldoInicial;

    @Column(name = "saldo_final", precision = 15, scale = 2)
    public BigDecimal saldoFinal;

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
}
