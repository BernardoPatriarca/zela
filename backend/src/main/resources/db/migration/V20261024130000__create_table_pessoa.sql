create table principal.pessoa (
    id UUID primary key default gen_random_uuid(),
    usuario_id UUID not null references principal.usuario(id),
    nome VARCHAR(150) not null,
    cpf CHAR(11),
    data_nascimento DATE not null,
    endereco VARCHAR(255),
    observacao VARCHAR(1000),
    criado_em TIMESTAMPTZ not null default now(),
    atualizado_em TIMESTAMPTZ not null default now(),
    excluido_em TIMESTAMPTZ
);

create unique index uk_pessoa_cpf_ativo on principal.pessoa (usuario_id, cpf) where cpf is not null and excluido_em is null;

create index idx_pessoa_usuario on principal.pessoa (usuario_id) where excluido_em is null;

create table auditoria.pessoa_aud (
    id UUID not null,
    rev BIGINT not null references auditoria.revisao(id),
    revtype smallint,
    usuario_id UUID,
    nome VARCHAR(150),
    cpf CHAR(11),
    data_nascimento DATE,
    endereco VARCHAR(255),
    observacao VARCHAR(1000),
    criado_em TIMESTAMPTZ,
    atualizado_em TIMESTAMPTZ,
    excluido_em TIMESTAMPTZ,
    primary key (id, rev)
);