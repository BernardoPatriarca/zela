package br.edu.unimater.zela.contabancaria;

import br.edu.unimater.zela.banco.Banco;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.envers.Audited;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Audited
@Table(name = "conta_bancaria", schema = "principal")
public class ContaBancaria {

    @Id
    @GeneratedValue
    public UUID id;

    @Column(name = "caso_id", nullable = false, updatable = false)
    public UUID casoId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "banco_id", nullable = false)
    public Banco banco;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    public TipoConta tipo;

    @Column(nullable = false, length = 10)
    public String agencia;

    @Column(nullable = false, length = 20)
    public String numero;

    @Column(length = 100)
    public String descricao;

    @Column(name = "saldo_inicial", nullable = false, precision = 15, scale = 2)
    public BigDecimal saldoInicial;

    @Column(name = "data_saldo_inicial", nullable = false)
    public LocalDate dataSaldoInicial;

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
