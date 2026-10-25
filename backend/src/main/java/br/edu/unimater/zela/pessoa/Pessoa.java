package br.edu.unimater.zela.pessoa;

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
@Table(name = "pessoa", schema = "principal")
public class Pessoa {

    @Id
    @GeneratedValue
    public UUID id;

    @Column(name = "usuario_id", nullable = false, updatable = false)
    public UUID usuarioId;

    @Column(nullable = false, length = 150)
    public String nome;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(length = 11)
    public String cpf;

    @Column(name = "data_nascimento", nullable = false)
    public LocalDate dataNascimento;

    @Column(length = 255)
    public String endereco;

    @Column(length = 1000)
    public String observacao;

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