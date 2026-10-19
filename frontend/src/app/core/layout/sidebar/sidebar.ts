import { Component, input } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';

@Component({
  selector: 'app-sidebar',
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.scss',
})
export class Sidebar {
  readonly aberto = input(true);

  itens = [
    { label: 'Painel', icon: 'pi pi-home', rota: '/dashboard' },
    { label: 'Casos', icon: 'pi pi-briefcase', rota: '/casos' },
    { label: 'Lançamentos', icon: 'pi pi-wallet', rota: '/lancamentos' },
    { label: 'Conciliação', icon: 'pi pi-sync', rota: '/conciliacao' },
    { label: 'Relatórios', icon: 'pi pi-file-pdf', rota: '/relatorios' },
    { label: 'Prazos', icon: 'pi pi-calendar', rota: '/prazos' },
    { label: 'Histórico', icon: 'pi pi-history', rota: '/historico' },
  ];
}
