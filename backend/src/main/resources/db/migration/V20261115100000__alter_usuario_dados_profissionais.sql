alter table principal.usuario
    add column nome_completo VARCHAR(150),
    add column oab_numero VARCHAR(10) check (oab_numero ~ '^[0-9]{1,7}$'),
    add column oab_uf CHAR(2) check (oab_uf ~ '^[A-Z]{2}$'),
    add check ((oab_numero is null) = (oab_uf is null));
