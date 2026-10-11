import { Component, computed, inject, signal } from '@angular/core';
import { Router, RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { AuthService } from '../../auth/auth-service';

@Component({
  selector: 'app-shell',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, ButtonModule],
  templateUrl: './shell.html',
  styleUrl: './shell.scss',
})
export class Shell {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  menuAberto = signal(true);

  readonly nomeUsuario = computed(() => {
    const usuario = this.auth.usuario();
    return usuario?.user_metadata?.['nome'] ?? usuario?.email ?? '';
  });

  itens = [
    { label: 'Painel', icon: 'pi pi-home', rota: '/dashboard' },
    { label: 'Casos', icon: 'pi pi-briefcase', rota: '/casos' },
    { label: 'Lançamentos', icon: 'pi pi-wallet', rota: '/lancamentos' },
    { label: 'Conciliação', icon: 'pi pi-sync', rota: '/conciliacao' },
    { label: 'Relatórios', icon: 'pi pi-file-pdf', rota: '/relatorios' },
    { label: 'Prazos', icon: 'pi pi-calendar', rota: '/prazos' },
    { label: 'Histórico', icon: 'pi pi-history', rota: '/historico' },
  ];

  async sair(): Promise<void> {
    await this.auth.sair();
    await this.router.navigateByUrl('/login');
  }
}
