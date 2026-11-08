create table principal.categoria (
    id UUID primary key default gen_random_uuid(),
    usuario_id UUID references principal.usuario(id),
    nome VARCHAR(100) not null,
    tipo VARCHAR(10) not null check (tipo in ('RECEITA', 'DESPESA')),
    criado_em TIMESTAMPTZ not null default now(),
    atualizado_em TIMESTAMPTZ not null default now(),
    excluido_em TIMESTAMPTZ
);

create unique index uk_categoria_nome_ativo
    on principal.categoria (coalesce(usuario_id, '00000000-0000-0000-0000-000000000000'), tipo, lower(nome))
    where excluido_em is null;

create index idx_categoria_usuario on principal.categoria (usuario_id) where excluido_em is null;

create table auditoria.categoria_aud (
    id UUID not null,
    rev BIGINT not null references auditoria.revisao(id),
    revtype smallint,
    usuario_id UUID,
    nome VARCHAR(100),
    tipo VARCHAR(10),
    criado_em TIMESTAMPTZ,
    atualizado_em TIMESTAMPTZ,
    excluido_em TIMESTAMPTZ,
    primary key (id, rev)
);
