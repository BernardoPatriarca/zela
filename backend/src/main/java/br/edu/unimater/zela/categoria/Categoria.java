package br.edu.unimater.zela.categoria;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.envers.Audited;

import java.time.Instant;
import java.util.UUID;

@Entity
@Audited
@Table(name = "categoria", schema = "principal")
public class Categoria {

    @Id
    @GeneratedValue
    public UUID id;

    @Column(name = "usuario_id", updatable = false)
    public UUID usuarioId;

    @Column(nullable = false, length = 100)
    public String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10, updatable = false)
    public TipoCategoria tipo;

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
