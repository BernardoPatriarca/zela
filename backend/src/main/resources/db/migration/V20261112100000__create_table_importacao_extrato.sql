create table principal.importacao_extrato (
    id UUID primary key default gen_random_uuid(),
    conta_bancaria_id UUID not null references principal.conta_bancaria(id),
    nome_arquivo VARCHAR(255) not null,
    tamanho_bytes INTEGER not null check (tamanho_bytes > 0),
    hash_arquivo VARCHAR(64) not null,
    banco_ofx VARCHAR(20),
    agencia_ofx VARCHAR(30),
    conta_ofx VARCHAR(30),
    data_inicio DATE not null,
    data_fim DATE not null,
    saldo_final NUMERIC(15, 2),
    data_saldo_final DATE,
    total INTEGER not null check (total >= 0),
    importadas INTEGER not null check (importadas >= 0),
    duplicadas INTEGER not null check (duplicadas >= 0),
    ignoradas INTEGER not null check (ignoradas >= 0),
    justificativa_exclusao VARCHAR(500),
    criado_em TIMESTAMPTZ not null default now(),
    atualizado_em TIMESTAMPTZ not null default now(),
    excluido_em TIMESTAMPTZ,
    check (data_fim >= data_inicio),
    check (total = importadas + duplicadas + ignoradas),
    check (excluido_em is null or justificativa_exclusao is not null)
);

create unique index uk_importacao_extrato_arquivo
    on principal.importacao_extrato (conta_bancaria_id, hash_arquivo)
    where excluido_em is null;

create index idx_importacao_extrato_conta_periodo
    on principal.importacao_extrato (conta_bancaria_id, data_inicio, data_fim)
    where excluido_em is null;

create table auditoria.importacao_extrato_aud (
    id UUID not null,
    rev BIGINT not null references auditoria.revisao(id),
    revtype smallint,
    conta_bancaria_id UUID,
    nome_arquivo VARCHAR(255),
    tamanho_bytes INTEGER,
    hash_arquivo VARCHAR(64),
    banco_ofx VARCHAR(20),
    agencia_ofx VARCHAR(30),
    conta_ofx VARCHAR(30),
    data_inicio DATE,
    data_fim DATE,
    saldo_final NUMERIC(15, 2),
    data_saldo_final DATE,
    total INTEGER,
    importadas INTEGER,
    duplicadas INTEGER,
    ignoradas INTEGER,
    justificativa_exclusao VARCHAR(500),
    criado_em TIMESTAMPTZ,
    atualizado_em TIMESTAMPTZ,
    excluido_em TIMESTAMPTZ,
    primary key (id, rev)
);
