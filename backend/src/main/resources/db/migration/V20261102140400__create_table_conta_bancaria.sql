create table principal.conta_bancaria (
    id UUID primary key default gen_random_uuid(),
    caso_id UUID not null references principal.caso(id),
    banco VARCHAR(30) not null,
    tipo VARCHAR(10) not null check (tipo in ('CORRENTE', 'POUPANCA', 'PAGAMENTO')),
    agencia VARCHAR(10) not null,
    numero VARCHAR(20) not null,
    descricao VARCHAR(100),
    saldo_inicial NUMERIC(15, 2) not null,
    data_saldo_inicial DATE not null,
    criado_em TIMESTAMPTZ not null default now(),
    atualizado_em TIMESTAMPTZ not null default now(),
    excluido_em TIMESTAMPTZ
);

create unique index uk_conta_bancaria_ativa
    on principal.conta_bancaria (caso_id, banco, agencia, numero)
    where excluido_em is null;

create index idx_conta_bancaria_caso on principal.conta_bancaria (caso_id) where excluido_em is null;

create table auditoria.conta_bancaria_aud (
    id UUID not null,
    rev BIGINT not null references auditoria.revisao(id),
    revtype smallint,
    caso_id UUID,
    banco VARCHAR(30),
    tipo VARCHAR(10),
    agencia VARCHAR(10),
    numero VARCHAR(20),
    descricao VARCHAR(100),
    saldo_inicial NUMERIC(15, 2),
    data_saldo_inicial DATE,
    criado_em TIMESTAMPTZ,
    atualizado_em TIMESTAMPTZ,
    excluido_em TIMESTAMPTZ,
    primary key (id, rev)
);
