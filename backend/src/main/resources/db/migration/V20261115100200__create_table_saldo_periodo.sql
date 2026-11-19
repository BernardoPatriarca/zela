create table principal.saldo_periodo (
    id UUID primary key default gen_random_uuid(),
    periodo_id UUID not null references principal.periodo_prestacao(id),
    conta_bancaria_id UUID not null references principal.conta_bancaria(id),
    saldo_inicial NUMERIC(15, 2) not null,
    saldo_final NUMERIC(15, 2),
    criado_em TIMESTAMPTZ not null default now(),
    atualizado_em TIMESTAMPTZ not null default now(),
    excluido_em TIMESTAMPTZ
);

create unique index uk_saldo_periodo_conta
    on principal.saldo_periodo (periodo_id, conta_bancaria_id)
    where excluido_em is null;

create index idx_saldo_periodo_conta on principal.saldo_periodo (conta_bancaria_id) where excluido_em is null;

create table auditoria.saldo_periodo_aud (
    id UUID not null,
    rev BIGINT not null references auditoria.revisao(id),
    revtype smallint,
    periodo_id UUID,
    conta_bancaria_id UUID,
    saldo_inicial NUMERIC(15, 2),
    saldo_final NUMERIC(15, 2),
    criado_em TIMESTAMPTZ,
    atualizado_em TIMESTAMPTZ,
    excluido_em TIMESTAMPTZ,
    primary key (id, rev)
);
