alter table principal.lancamento
    add column emitente_nome VARCHAR(150),
    add column emitente_documento VARCHAR(14) check (emitente_documento ~ '^([0-9]{11}|[0-9]{14})$');

alter table auditoria.lancamento_aud
    add column emitente_nome VARCHAR(150),
    add column emitente_documento VARCHAR(14);
