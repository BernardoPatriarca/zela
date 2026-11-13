alter table principal.movimentacao_extrato
    add column situacao VARCHAR(10) not null default 'PENDENTE'
        check (situacao in ('PENDENTE', 'CONCILIADA', 'IGNORADA')),
    add column forma_conciliacao VARCHAR(10)
        check (forma_conciliacao in ('AUTOMATICA', 'MANUAL')),
    add column conciliada_em TIMESTAMPTZ,
    add column justificativa VARCHAR(500),
    add check (
        (situacao = 'CONCILIADA' and lancamento_id is not null)
        or (situacao in ('PENDENTE', 'IGNORADA') and lancamento_id is null)
    );

create unique index uk_movimentacao_extrato_lancamento
    on principal.movimentacao_extrato (lancamento_id)
    where lancamento_id is not null and excluido_em is null;

alter table auditoria.movimentacao_extrato_aud
    add column situacao VARCHAR(10),
    add column forma_conciliacao VARCHAR(10),
    add column conciliada_em TIMESTAMPTZ,
    add column justificativa VARCHAR(500);
