alter table principal.periodo_prestacao
    add column situacao VARCHAR(20) not null default 'AGUARDANDO_CURADOR'
        check (situacao in ('AGUARDANDO_CURADOR', 'EM_REVISAO', 'PRONTO', 'ENTREGUE')),
    add column prazo_curador DATE,
    add column notas_explicativas VARCHAR(4000);

update principal.periodo_prestacao set situacao = 'ENTREGUE' where entregue_em is not null;

alter table principal.periodo_prestacao
    add check ((situacao = 'ENTREGUE') = (entregue_em is not null));

create index idx_periodo_entregue
    on principal.periodo_prestacao (caso_id, data_inicio, data_fim)
    where entregue_em is not null and excluido_em is null;

alter table auditoria.periodo_prestacao_aud
    add column situacao VARCHAR(20),
    add column prazo_curador DATE,
    add column notas_explicativas VARCHAR(4000);
