create table principal.justificativa_pendencia (
    id UUID primary key default gen_random_uuid(),
    periodo_id UUID not null references principal.periodo_prestacao(id),
    tipo VARCHAR(30) not null check (tipo in (
        'MOVIMENTACAO_PENDENTE',
        'LANCAMENTO_SEM_EXTRATO',
        'EXTRATO_FALTANDO',
        'SALDO_DIVERGENTE',
        'SALDO_INICIAL_DIFERENTE',
        'POSSIVEL_DUPLICIDADE'
    )),
    referencia VARCHAR(200) not null,
    texto VARCHAR(1000) not null,
    criado_em TIMESTAMPTZ not null default now(),
    atualizado_em TIMESTAMPTZ not null default now(),
    excluido_em TIMESTAMPTZ
);

create unique index uk_justificativa_pendencia_ativa
    on principal.justificativa_pendencia (periodo_id, tipo, referencia)
    where excluido_em is null;

create index idx_justificativa_pendencia_periodo
    on principal.justificativa_pendencia (periodo_id) where excluido_em is null;

create table auditoria.justificativa_pendencia_aud (
    id UUID not null,
    rev BIGINT not null references auditoria.revisao(id),
    revtype smallint,
    periodo_id UUID,
    tipo VARCHAR(30),
    referencia VARCHAR(200),
    texto VARCHAR(1000),
    criado_em TIMESTAMPTZ,
    atualizado_em TIMESTAMPTZ,
    excluido_em TIMESTAMPTZ,
    primary key (id, rev)
);
