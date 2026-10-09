import { Component, signal } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { ButtonModule } from 'primeng/button';

@Component({
  selector: 'app-shell',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, ButtonModule],
  templateUrl: './shell.html',
  styleUrl: './shell.scss'
})
export class Shell {
  menuAberto = signal(true);

  itens = [
    { label: 'Painel', icon: 'pi pi-home', rota: '/dashboard' },
    { label: 'Casos', icon: 'pi pi-briefcase', rota: '/casos' },
    { label: 'Lançamentos', icon: 'pi pi-wallet', rota: '/lancamentos' },
    { label: 'Conciliação', icon: 'pi pi-sync', rota: '/conciliacao' },
    { label: 'Relatórios', icon: 'pi pi-file-pdf', rota: '/relatorios' },
    { label: 'Prazos', icon: 'pi pi-calendar', rota: '/prazos' },
    { label: 'Histórico', icon: 'pi pi-history', rota: '/historico' }
  ];
}