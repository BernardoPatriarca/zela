package br.edu.unimater.zela.importacaoextrato;

import br.edu.unimater.zela.categoria.TipoCategoria;
import br.edu.unimater.zela.contabancaria.ContaBancaria;
import br.edu.unimater.zela.lancamento.Lancamento;
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
@Table(name = "movimentacao_extrato", schema = "principal")
public class MovimentacaoExtrato {

    @Id
    @GeneratedValue
    public UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "importacao_id", nullable = false, updatable = false)
    public ImportacaoExtrato importacao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conta_bancaria_id", nullable = false, updatable = false)
    public ContaBancaria contaBancaria;

    @Column(name = "data_movimentacao", nullable = false)
    public LocalDate dataMovimentacao;

    @Column(nullable = false, precision = 15, scale = 2)
    public BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    public TipoCategoria tipo;

    @Column(name = "tipo_ofx", length = 20)
    public String tipoOfx;

    @Column(length = 255)
    public String fitid;

    @Column(nullable = false, length = 64)
    public String hash;

    @Column(nullable = false, length = 255)
    public String descricao;

    @Column(name = "numero_documento", length = 50)
    public String numeroDocumento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lancamento_id")
    public Lancamento lancamento;

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
