create table auditoria.revisao (
    id BIGINT generated always as identity primary key,
    timestamp BIGINT not null,
    usuario_id UUID references principal.usuario(id)
);

create table auditoria.usuario_aud (
    id UUID not null,
    rev BIGINT not null references auditoria.revisao(id),
    revtype smallint,
    nome VARCHAR(150),
    email VARCHAR(255),
    criado_em TIMESTAMPTZ,
    atualizado_em TIMESTAMPTZ,
    excluido_em TIMESTAMPTZ,
    primary key (id, rev)
);