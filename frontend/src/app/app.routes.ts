import { Routes } from '@angular/router';
import { authGuard, visitanteGuard } from './core/auth/auth-guard';

export const routes: Routes = [
  {
    title: 'Login',
    path: 'login',
    canActivate: [visitanteGuard],
    loadComponent: () => import('./features/auth/login/login').then((m) => m.Login),
  },
  {
    title: 'Cadastro',
    path: 'register',
    canActivate: [visitanteGuard],
    loadComponent: () => import('./features/auth/register/register').then((m) => m.Register),
  },
  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () => import('./core/layout/shell/shell').then((m) => m.Shell),

    children: [
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'dashboard',
      },
      {
        path: 'dashboard',
        title: 'Painel',
        loadComponent: () =>
          import('./features/dashboard/dashboard-page/dashboard-page').then((m) => m.DashboardPage),
      },
      {
        path: 'pessoas',
        title: 'Pessoas',
        loadComponent: () =>
          import('./features/pessoas/pessoas-page/pessoas-page').then((m) => m.PessoasPage),
      },
      {
        path: 'casos',
        title: 'Casos',
        loadComponent: () =>
          import('./features/casos/casos-page/casos-page').then((m) => m.CasosPage),
      },
      {
        path: 'contas-bancarias',
        title: 'Contas bancárias',
        loadComponent: () =>
          import('./features/contas-bancarias/contas-bancarias-page/contas-bancarias-page').then(
            (m) => m.ContasBancariasPage,
          ),
      },
      {
        path: 'categorias',
        title: 'Categorias',
        loadComponent: () =>
          import('./features/categorias/categorias-page/categorias-page').then(
            (m) => m.CategoriasPage,
          ),
      },
      {
        path: 'lancamentos',
        title: 'Lançamentos',
        loadComponent: () =>
          import('./features/lancamentos/lancamentos-page/lancamentos-page').then(
            (m) => m.LancamentosPage,
          ),
      },
      {
        path: 'importacoes-extrato',
        title: 'Importar extrato',
        loadComponent: () =>
          import('./features/importacoes-extrato/importacoes-extrato-page/importacoes-extrato-page').then(
            (m) => m.ImportacoesExtratoPage,
          ),
      },
      {
        path: 'conciliacao',
        title: 'Conciliação',
        loadComponent: () =>
          import('./features/conciliacao/conciliacao-page/conciliacao-page').then(
            (m) => m.ConciliacaoPage,
          ),
      },
      {
        path: 'relatorios',
        title: 'Relatórios',
        loadComponent: () =>
          import('./features/relatorios/relatorios-page/relatorios-page').then(
            (m) => m.RelatoriosPage,
          ),
      },
      {
        path: 'prazos',
        title: 'Prazos',
        loadComponent: () =>
          import('./features/prazos/prazos-page/prazos-page').then((m) => m.PrazosPage),
      },
      {
        path: 'historico',
        title: 'Histórico',
        loadComponent: () =>
          import('./features/historico/historico-page/historico-page').then((m) => m.HistoricoPage),
      },
      {
        path: 'comarcas',
        title: 'Comarcas',
        loadComponent: () =>
          import('./features/comarcas/comarcas-page/comarcas-page').then((m) => m.ComarcasPage),
      },
      {
        path: 'varas',
        title: 'Varas',
        loadComponent: () =>
          import('./features/varas/varas-page/varas-page').then((m) => m.VarasPage),
      },
    ],
  },
  { path: '**', redirectTo: '' },
];
