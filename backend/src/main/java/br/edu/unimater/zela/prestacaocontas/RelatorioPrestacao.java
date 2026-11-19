package br.edu.unimater.zela.prestacaocontas;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.envers.Audited;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Audited
@Table(name = "relatorio_prestacao", schema = "principal")
public class RelatorioPrestacao {

    @Id
    @GeneratedValue
    public UUID id;

    @Column(name = "periodo_id", nullable = false, updatable = false)
    public UUID periodoId;

    @Column(nullable = false, updatable = false)
    public int versao;

    @CreationTimestamp
    @Column(name = "gerado_em", nullable = false, updatable = false)
    public Instant geradoEm;

    @Column(name = "gerado_por", nullable = false, updatable = false)
    public UUID geradoPor;

    @Column(name = "chave_objeto", nullable = false, updatable = false, length = 255)
    public String chaveObjeto;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "hash_sha256", nullable = false, updatable = false, length = 64)
    public String hashSha256;

    @Column(name = "tamanho_bytes", nullable = false, updatable = false)
    public int tamanhoBytes;

    @Column(name = "com_ressalvas", nullable = false, updatable = false)
    public boolean comRessalvas;

    @Column(name = "quantidade_pendencias", nullable = false, updatable = false)
    public int quantidadePendencias;

    @Column(name = "saldo_final_total", nullable = false, updatable = false, precision = 15, scale = 2)
    public BigDecimal saldoFinalTotal;
}
