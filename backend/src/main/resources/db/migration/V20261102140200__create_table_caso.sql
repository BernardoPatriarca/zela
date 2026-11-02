create table principal.caso (
    id UUID primary key default gen_random_uuid(),
    usuario_id UUID not null references principal.usuario(id),
    tipo VARCHAR(10) not null check (tipo in ('CURATELA', 'TUTELA')),
    numero_processo CHAR(20) not null check (numero_processo ~ '^[0-9]{20}$'),
    vara_id UUID not null references principal.vara(id),
    pessoa_protegida_id UUID not null references principal.pessoa(id),
    responsavel_id UUID not null references principal.pessoa(id),
    periodicidade_meses INTEGER not null check (periodicidade_meses between 1 and 60),
    prazo_entrega_dias INTEGER not null check (prazo_entrega_dias between 0 and 365),
    encerrado_em DATE,
    criado_em TIMESTAMPTZ not null default now(),
    atualizado_em TIMESTAMPTZ not null default now(),
    excluido_em TIMESTAMPTZ,
    check (pessoa_protegida_id <> responsavel_id)
);

create index idx_caso_usuario on principal.caso (usuario_id) where excluido_em is null;

create index idx_caso_vara on principal.caso (vara_id) where excluido_em is null;

create index idx_caso_pessoa_protegida on principal.caso (pessoa_protegida_id) where excluido_em is null;

create index idx_caso_responsavel on principal.caso (responsavel_id) where excluido_em is null;

create table auditoria.caso_aud (
    id UUID not null,
    rev BIGINT not null references auditoria.revisao(id),
    revtype smallint,
    usuario_id UUID,
    tipo VARCHAR(10),
    numero_processo CHAR(20),
    vara_id UUID,
    pessoa_protegida_id UUID,
    responsavel_id UUID,
    periodicidade_meses INTEGER,
    prazo_entrega_dias INTEGER,
    encerrado_em DATE,
    criado_em TIMESTAMPTZ,
    atualizado_em TIMESTAMPTZ,
    excluido_em TIMESTAMPTZ,
    primary key (id, rev)
);
