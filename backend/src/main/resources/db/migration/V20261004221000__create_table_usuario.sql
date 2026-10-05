create table principal.usuario (
    id UUID primary key default gen_random_uuid(),
    nome VARCHAR(150) not null,
    email VARCHAR(255) not null,
    criado_em TIMESTAMPTZ not null default now(),
    atualizado_em TIMESTAMPTZ not null default now(),
    excluido_em TIMESTAMPTZ
);

create unique index uk_usuario_email_ativo
    on principal.usuario (lower(email)) where excluido_em is null;