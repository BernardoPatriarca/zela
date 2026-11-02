insert into principal.vara (comarca_id, nome)
select c.id, v.nome
from principal.comarca c
cross join (values
    ('Vara de Família e Sucessões'),
    ('1ª Vara Cível e da Fazenda Pública'),
    ('2ª Vara Cível e da Fazenda Pública')
) as v(nome)
where c.nome = 'Pato Branco' and c.uf = 'PR' and c.usuario_id is null;

insert into principal.vara (comarca_id, nome)
select c.id, v.nome
from principal.comarca c
cross join (values
    ('Vara de Família e Sucessões'),
    ('Vara Cível')
) as v(nome)
where c.uf = 'PR'
  and c.usuario_id is null
  and c.nome in (
      'Ampére',
      'Barracão',
      'Capanema',
      'Chopinzinho',
      'Clevelândia',
      'Coronel Vivida',
      'Dois Vizinhos',
      'Francisco Beltrão',
      'Mangueirinha',
      'Marmeleiro',
      'Palmas',
      'Realeza',
      'Salto do Lontra',
      'Santo Antônio do Sudoeste',
      'São João'
  );
