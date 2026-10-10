# ZELA

Plataforma web de gestão financeira e prestação de contas para curatela e tutela.
Permite registrar casos, lançamentos e prazos, conciliar extratos bancários e gerar
relatórios para a prestação de contas, mantendo o histórico das alterações.

Projeto de Trabalho de Conclusão de Curso (Bacharelado em Sistemas de Informação,
UNIMATER).

## Frontend

Angular 21 + PrimeNG 21 (tema Aura).

    npm install
    ng serve        # http://localhost:4200

## Configuração do ambiente

Os arquivos `src/environments/environment.ts` e `environment.development.ts` não vão para o Git. Para criá-los:

```bash
cp src/environments/environment.example.ts src/environments/environment.ts
cp src/environments/environment.example.ts src/environments/environment.development.ts
```

Preencha `supabaseUrl` e `supabaseAnonKey` com os valores do Supabase (Connect → Framework). Em desenvolvimento, `apiUrl` é `/api`.

O `ng serve` usa o `environment.development.ts` e o `ng build` usa o `environment.ts`.