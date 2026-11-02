create table principal.vara (
    id UUID primary key default gen_random_uuid(),
    usuario_id UUID references principal.usuario(id),
    comarca_id UUID not null references principal.comarca(id),
    nome VARCHAR(150) not null,
    criado_em TIMESTAMPTZ not null default now(),
    atualizado_em TIMESTAMPTZ not null default now(),
    excluido_em TIMESTAMPTZ
);

create unique index uk_vara_nome_ativo
    on principal.vara (coalesce(usuario_id, '00000000-0000-0000-0000-000000000000'), comarca_id, lower(nome))
    where excluido_em is null;

create index idx_vara_comarca on principal.vara (comarca_id) where excluido_em is null;

create table auditoria.vara_aud (
    id UUID not null,
    rev BIGINT not null references auditoria.revisao(id),
    revtype smallint,
    usuario_id UUID,
    comarca_id UUID,
    nome VARCHAR(150),
    criado_em TIMESTAMPTZ,
    atualizado_em TIMESTAMPTZ,
    excluido_em TIMESTAMPTZ,
    primary key (id, rev)
);
