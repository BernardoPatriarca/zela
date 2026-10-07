package br.edu.unimater.zela.shared.audit;

import jakarta.persistence.*;
import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;

import java.util.UUID;

@Entity
@Table(name = "revisao", schema = "auditoria")
@RevisionEntity(RevisaoListener.class)
public class Revisao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @RevisionNumber
    @Column(name = "id")
    public long id;

    @RevisionTimestamp
    @Column(name = "timestamp", nullable = false)
    public long timestamp;

    @Column(name = "usuario_id")
    public UUID usuarioId;
}