package br.edu.unimater.zela.prestacaocontas;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.envers.Audited;

import java.time.Instant;
import java.util.UUID;

@Entity
@Audited
@Table(name = "justificativa_pendencia", schema = "principal")
public class JustificativaPendencia {

    @Id
    @GeneratedValue
    public UUID id;

    @Column(name = "periodo_id", nullable = false, updatable = false)
    public UUID periodoId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 30)
    public TipoPendencia tipo;

    @Column(nullable = false, updatable = false, length = 200)
    public String referencia;

    @Column(nullable = false, length = 1000)
    public String texto;

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
