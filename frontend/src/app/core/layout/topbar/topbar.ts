import { Component, computed, inject, output } from '@angular/core';
import { Router } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { AuthService } from '../../auth/auth-service';

@Component({
  selector: 'app-topbar',
  imports: [ButtonModule],
  templateUrl: './topbar.html',
  styleUrl: './topbar.scss',
})
export class Topbar {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  readonly alternarMenu = output<void>();
  readonly suporteUrl = 'https://wa.me/5546988120972';

  readonly nomeUsuario = computed(() => {
    const usuario = this.auth.usuario();
    return usuario?.user_metadata?.['nome'] ?? usuario?.email ?? '';
  });

  async sair(): Promise<void> {
    await this.auth.sair();
    await this.router.navigateByUrl('/login');
  }
}
