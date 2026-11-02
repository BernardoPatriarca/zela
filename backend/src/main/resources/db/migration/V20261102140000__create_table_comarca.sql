create table principal.comarca (
    id UUID primary key default gen_random_uuid(),
    usuario_id UUID references principal.usuario(id),
    nome VARCHAR(100) not null,
    uf CHAR(2) not null,
    criado_em TIMESTAMPTZ not null default now(),
    atualizado_em TIMESTAMPTZ not null default now(),
    excluido_em TIMESTAMPTZ
);

create unique index uk_comarca_nome_ativo
    on principal.comarca (coalesce(usuario_id, '00000000-0000-0000-0000-000000000000'), lower(nome), uf)
    where excluido_em is null;

create index idx_comarca_usuario on principal.comarca (usuario_id) where excluido_em is null;

create table auditoria.comarca_aud (
    id UUID not null,
    rev BIGINT not null references auditoria.revisao(id),
    revtype smallint,
    usuario_id UUID,
    nome VARCHAR(100),
    uf CHAR(2),
    criado_em TIMESTAMPTZ,
    atualizado_em TIMESTAMPTZ,
    excluido_em TIMESTAMPTZ,
    primary key (id, rev)
);
