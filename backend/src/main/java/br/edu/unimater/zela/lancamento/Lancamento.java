package br.edu.unimater.zela.lancamento;

import br.edu.unimater.zela.categoria.Categoria;
import br.edu.unimater.zela.categoria.TipoCategoria;
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
@Table(name = "lancamento", schema = "principal")
public class Lancamento {

    @Id
    @GeneratedValue
    public UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conta_bancaria_id", nullable = false)
    public ContaBancaria contaBancaria;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    public Categoria categoria;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10, updatable = false)
    public TipoCategoria tipo;

    @Column(name = "data_lancamento", nullable = false)
    public LocalDate dataLancamento;

    @Column(nullable = false, precision = 15, scale = 2)
    public BigDecimal valor;

    @Column(nullable = false, length = 255)
    public String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    public SituacaoLancamento situacao;

    @Column(name = "justificativa_alteracao", length = 500)
    public String justificativaAlteracao;

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
