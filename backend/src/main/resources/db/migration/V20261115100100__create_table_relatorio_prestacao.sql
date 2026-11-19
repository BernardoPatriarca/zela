create table principal.relatorio_prestacao (
    id UUID primary key default gen_random_uuid(),
    periodo_id UUID not null references principal.periodo_prestacao(id),
    versao INTEGER not null check (versao > 0),
    gerado_em TIMESTAMPTZ not null default now(),
    gerado_por UUID not null references principal.usuario(id),
    chave_objeto VARCHAR(255) not null,
    hash_sha256 CHAR(64) not null,
    tamanho_bytes INTEGER not null check (tamanho_bytes > 0),
    com_ressalvas BOOLEAN not null,
    quantidade_pendencias INTEGER not null check (quantidade_pendencias >= 0),
    saldo_final_total NUMERIC(15, 2) not null,
    check (com_ressalvas = (quantidade_pendencias > 0))
);

create unique index uk_relatorio_prestacao_versao
    on principal.relatorio_prestacao (periodo_id, versao);

create table auditoria.relatorio_prestacao_aud (
    id UUID not null,
    rev BIGINT not null references auditoria.revisao(id),
    revtype smallint,
    periodo_id UUID,
    versao INTEGER,
    gerado_em TIMESTAMPTZ,
    gerado_por UUID,
    chave_objeto VARCHAR(255),
    hash_sha256 CHAR(64),
    tamanho_bytes INTEGER,
    com_ressalvas BOOLEAN,
    quantidade_pendencias INTEGER,
    saldo_final_total NUMERIC(15, 2),
    primary key (id, rev)
);
