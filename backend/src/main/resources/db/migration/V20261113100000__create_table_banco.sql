create table principal.banco (
    id UUID primary key default gen_random_uuid(),
    usuario_id UUID references principal.usuario(id),
    codigo VARCHAR(3) not null,
    nome VARCHAR(100) not null,
    criado_em TIMESTAMPTZ not null default now(),
    atualizado_em TIMESTAMPTZ not null default now(),
    excluido_em TIMESTAMPTZ
);

create unique index uk_banco_codigo_ativo
    on principal.banco (coalesce(usuario_id, '00000000-0000-0000-0000-000000000000'), codigo)
    where excluido_em is null;

create index idx_banco_usuario on principal.banco (usuario_id) where excluido_em is null;

create table auditoria.banco_aud (
    id UUID not null,
    rev BIGINT not null references auditoria.revisao(id),
    revtype smallint,
    usuario_id UUID,
    codigo VARCHAR(3),
    nome VARCHAR(100),
    criado_em TIMESTAMPTZ,
    atualizado_em TIMESTAMPTZ,
    excluido_em TIMESTAMPTZ,
    primary key (id, rev)
);
