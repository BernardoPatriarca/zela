package br.edu.unimater.zela.importacaoextrato;

import br.edu.unimater.zela.contabancaria.ContaBancaria;
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
@Table(name = "importacao_extrato", schema = "principal")
public class ImportacaoExtrato {

    @Id
    @GeneratedValue
    public UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conta_bancaria_id", nullable = false)
    public ContaBancaria contaBancaria;

    @Column(name = "nome_arquivo", nullable = false, length = 255)
    public String nomeArquivo;

    @Column(name = "tamanho_bytes", nullable = false)
    public int tamanhoBytes;

    @Column(name = "hash_arquivo", nullable = false, length = 64)
    public String hashArquivo;

    @Column(name = "banco_ofx", length = 20)
    public String bancoOfx;

    @Column(name = "agencia_ofx", length = 30)
    public String agenciaOfx;

    @Column(name = "conta_ofx", length = 30)
    public String contaOfx;

    @Column(name = "data_inicio", nullable = false)
    public LocalDate dataInicio;

    @Column(name = "data_fim", nullable = false)
    public LocalDate dataFim;

    @Column(name = "saldo_final", precision = 15, scale = 2)
    public BigDecimal saldoFinal;

    @Column(name = "data_saldo_final")
    public LocalDate dataSaldoFinal;

    @Column(nullable = false)
    public int total;

    @Column(nullable = false)
    public int importadas;

    @Column(nullable = false)
    public int duplicadas;

    @Column(nullable = false)
    public int ignoradas;

    @Column(name = "justificativa_exclusao", length = 500)
    public String justificativaExclusao;

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
