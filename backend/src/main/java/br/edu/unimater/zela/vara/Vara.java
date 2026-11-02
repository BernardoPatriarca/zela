package br.edu.unimater.zela.vara;

import br.edu.unimater.zela.comarca.Comarca;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.envers.Audited;

import java.time.Instant;
import java.util.UUID;

@Entity
@Audited
@Table(name = "vara", schema = "principal")
public class Vara {

    @Id
    @GeneratedValue
    public UUID id;

    @Column(name = "usuario_id", updatable = false)
    public UUID usuarioId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comarca_id", nullable = false)
    public Comarca comarca;

    @Column(nullable = false, length = 150)
    public String nome;

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

    public boolean isSistema() {
        return usuarioId == null;
    }
}
