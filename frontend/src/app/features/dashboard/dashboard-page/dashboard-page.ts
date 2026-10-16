import { Component, inject, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { Button } from 'primeng/button';
import { Message } from 'primeng/message';
import { Skeleton } from 'primeng/skeleton';
import { UsuarioApi, UsuarioResponse } from '../../../core/api/usuario-api';

@Component({
  imports: [Button, Message, Skeleton],
  selector: 'app-dashboard-page',
  styleUrl: './dashboard-page.scss',
  templateUrl: './dashboard-page.html',
})
export class DashboardPage {
  private api = inject(UsuarioApi);

  usuario = signal<UsuarioResponse | null>(null);
  carregando = signal(true);
  erro = signal(false);

  constructor() {
    void this.carregar();
  }

  async carregar(): Promise<void> {
    this.carregando.set(true);
    this.erro.set(false);

    try {
      this.usuario.set(await firstValueFrom(this.api.obterPerfil()));
    } catch (e) {
      console.error('[dashboard] falha ao carregar o usuário', e);
      this.erro.set(true);
    } finally {
      this.carregando.set(false);
    }
  }
}
