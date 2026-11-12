insert into principal.banco (codigo, nome)
select '999', 'Outro'
where exists (select 1 from principal.conta_bancaria where banco = 'OUTRO');

create temporary table banco_legado (
    banco VARCHAR(30) primary key,
    codigo VARCHAR(3) not null
);

insert into banco_legado (banco, codigo) values
    ('BANCO_DO_BRASIL', '001'),
    ('SANTANDER', '033'),
    ('BANRISUL', '041'),
    ('INTER', '077'),
    ('CAIXA', '104'),
    ('CRESOL', '133'),
    ('BRADESCO', '237'),
    ('NUBANK', '260'),
    ('MERCADO_PAGO', '323'),
    ('C6', '336'),
    ('ITAU', '341'),
    ('SICREDI', '748'),
    ('SICOOB', '756'),
    ('OUTRO', '999');

alter table principal.conta_bancaria add column banco_id UUID references principal.banco(id);
alter table auditoria.conta_bancaria_aud add column banco_id UUID;

update principal.conta_bancaria c
set banco_id = b.id
from banco_legado l
join principal.banco b on b.codigo = l.codigo and b.usuario_id is null
where c.banco = l.banco;

update auditoria.conta_bancaria_aud a
set banco_id = b.id
from banco_legado l
join principal.banco b on b.codigo = l.codigo and b.usuario_id is null
where a.banco = l.banco;

drop table banco_legado;

alter table principal.conta_bancaria alter column banco_id set not null;

drop index principal.uk_conta_bancaria_ativa;

alter table principal.conta_bancaria drop column banco;
alter table auditoria.conta_bancaria_aud drop column banco;

create unique index uk_conta_bancaria_ativa
    on principal.conta_bancaria (caso_id, banco_id, agencia, numero)
    where excluido_em is null;
