create table principal.lancamento (
    id UUID primary key default gen_random_uuid(),
    conta_bancaria_id UUID not null references principal.conta_bancaria(id),
    categoria_id UUID not null references principal.categoria(id),
    tipo VARCHAR(10) not null check (tipo in ('RECEITA', 'DESPESA')),
    data_lancamento DATE not null,
    valor NUMERIC(15, 2) not null check (valor > 0),
    descricao VARCHAR(255) not null,
    situacao VARCHAR(10) not null default 'PENDENTE' check (situacao in ('PENDENTE', 'CONCILIADO')),
    justificativa_alteracao VARCHAR(500),
    criado_em TIMESTAMPTZ not null default now(),
    atualizado_em TIMESTAMPTZ not null default now(),
    excluido_em TIMESTAMPTZ,
    check (excluido_em is null or justificativa_alteracao is not null)
);

create index idx_lancamento_conta_data
    on principal.lancamento (conta_bancaria_id, data_lancamento desc) where excluido_em is null;

create index idx_lancamento_categoria on principal.lancamento (categoria_id) where excluido_em is null;

create table auditoria.lancamento_aud (
    id UUID not null,
    rev BIGINT not null references auditoria.revisao(id),
    revtype smallint,
    conta_bancaria_id UUID,
    categoria_id UUID,
    tipo VARCHAR(10),
    data_lancamento DATE,
    valor NUMERIC(15, 2),
    descricao VARCHAR(255),
    situacao VARCHAR(10),
    justificativa_alteracao VARCHAR(500),
    criado_em TIMESTAMPTZ,
    atualizado_em TIMESTAMPTZ,
    excluido_em TIMESTAMPTZ,
    primary key (id, rev)
);
