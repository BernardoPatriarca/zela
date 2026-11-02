create table principal.periodo_prestacao (
    id UUID primary key default gen_random_uuid(),
    caso_id UUID not null references principal.caso(id),
    data_inicio DATE not null,
    data_fim DATE not null,
    prazo_entrega DATE not null,
    entregue_em DATE,
    criado_em TIMESTAMPTZ not null default now(),
    atualizado_em TIMESTAMPTZ not null default now(),
    excluido_em TIMESTAMPTZ,
    check (data_fim >= data_inicio),
    check (prazo_entrega >= data_fim)
);

create unique index uk_periodo_aberto_por_caso
    on principal.periodo_prestacao (caso_id)
    where entregue_em is null and excluido_em is null;

create index idx_periodo_caso on principal.periodo_prestacao (caso_id) where excluido_em is null;

create table auditoria.periodo_prestacao_aud (
    id UUID not null,
    rev BIGINT not null references auditoria.revisao(id),
    revtype smallint,
    caso_id UUID,
    data_inicio DATE,
    data_fim DATE,
    prazo_entrega DATE,
    entregue_em DATE,
    criado_em TIMESTAMPTZ,
    atualizado_em TIMESTAMPTZ,
    excluido_em TIMESTAMPTZ,
    primary key (id, rev)
);
