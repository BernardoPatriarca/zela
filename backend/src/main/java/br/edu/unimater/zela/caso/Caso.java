package br.edu.unimater.zela.caso;

import br.edu.unimater.zela.pessoa.Pessoa;
import br.edu.unimater.zela.vara.Vara;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.envers.Audited;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Audited
@Table(name = "caso", schema = "principal")
public class Caso {

    @Id
    @GeneratedValue
    public UUID id;

    @Column(name = "usuario_id", nullable = false, updatable = false)
    public UUID usuarioId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    public TipoCaso tipo;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "numero_processo", nullable = false, length = 20)
    public String numeroProcesso;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vara_id", nullable = false)
    public Vara vara;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pessoa_protegida_id", nullable = false)
    public Pessoa pessoaProtegida;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "responsavel_id", nullable = false)
    public Pessoa responsavel;

    @Column(name = "periodicidade_meses", nullable = false)
    public int periodicidadeMeses;

    @Column(name = "prazo_entrega_dias", nullable = false)
    public int prazoEntregaDias;

    @Column(name = "encerrado_em")
    public LocalDate encerradoEm;

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

    public boolean isEncerrado() {
        return encerradoEm != null;
    }

    public SituacaoCaso getSituacao() {
        return isEncerrado() ? SituacaoCaso.ENCERRADO : SituacaoCaso.ATIVO;
    }
}
