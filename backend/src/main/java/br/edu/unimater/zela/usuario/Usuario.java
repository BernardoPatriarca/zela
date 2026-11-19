package br.edu.unimater.zela.usuario;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "usuario", schema = "principal")
public class Usuario {

    @Id
    public UUID id;

    @Column(nullable = false, length = 150)
    public String nome;

    @Column(nullable = false, length = 255)
    public String email;

    @Column(name = "nome_completo", length = 150)
    public String nomeCompleto;

    @Column(name = "oab_numero", length = 10)
    public String oabNumero;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "oab_uf", length = 2)
    public String oabUf;

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