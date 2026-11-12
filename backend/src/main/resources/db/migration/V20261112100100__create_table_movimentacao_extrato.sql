create table principal.movimentacao_extrato (
    id UUID primary key default gen_random_uuid(),
    importacao_id UUID not null references principal.importacao_extrato(id),
    conta_bancaria_id UUID not null references principal.conta_bancaria(id),
    data_movimentacao DATE not null,
    valor NUMERIC(15, 2) not null check (valor > 0),
    tipo VARCHAR(10) not null check (tipo in ('RECEITA', 'DESPESA')),
    tipo_ofx VARCHAR(20),
    fitid VARCHAR(255),
    hash VARCHAR(64) not null,
    descricao VARCHAR(255) not null,
    numero_documento VARCHAR(50),
    lancamento_id UUID references principal.lancamento(id),
    criado_em TIMESTAMPTZ not null default now(),
    atualizado_em TIMESTAMPTZ not null default now(),
    excluido_em TIMESTAMPTZ
);

create unique index uk_movimentacao_extrato_fitid
    on principal.movimentacao_extrato (conta_bancaria_id, fitid)
    where fitid is not null and excluido_em is null;

create unique index uk_movimentacao_extrato_hash
    on principal.movimentacao_extrato (conta_bancaria_id, hash)
    where fitid is null and excluido_em is null;

create index idx_movimentacao_extrato_importacao
    on principal.movimentacao_extrato (importacao_id) where excluido_em is null;

create table auditoria.movimentacao_extrato_aud (
    id UUID not null,
    rev BIGINT not null references auditoria.revisao(id),
    revtype smallint,
    importacao_id UUID,
    conta_bancaria_id UUID,
    data_movimentacao DATE,
    valor NUMERIC(15, 2),
    tipo VARCHAR(10),
    tipo_ofx VARCHAR(20),
    fitid VARCHAR(255),
    hash VARCHAR(64),
    descricao VARCHAR(255),
    numero_documento VARCHAR(50),
    lancamento_id UUID,
    criado_em TIMESTAMPTZ,
    atualizado_em TIMESTAMPTZ,
    excluido_em TIMESTAMPTZ,
    primary key (id, rev)
);
