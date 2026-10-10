import { Component, inject, signal } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { Button } from 'primeng/button';
import { InputText } from 'primeng/inputtext';
import { Message } from 'primeng/message';
import { Password } from 'primeng/password';
import { UsuarioApi } from '../../../core/api/usuario-api';
import { AuthService } from '../../../core/auth/auth-service';
import { criarRegisterForm } from '../forms/register.form';

@Component({
  imports: [ReactiveFormsModule, RouterLink, Button, InputText, Message, Password],
  selector: 'app-register',
  styleUrl: './register.scss',
  templateUrl: './register.html',
})
export class Register {
  private readonly auth = inject(AuthService);
  private readonly api = inject(UsuarioApi);
  private readonly router = inject(Router);

  readonly enviando = signal(false);
  readonly erro = signal<string | null>(null);

  readonly form = criarRegisterForm();

  get nome() {
    return this.form.controls.nome;
  }

  get email() {
    return this.form.controls.email;
  }

  get senha() {
    return this.form.controls.senha;
  }

  get confirmacao() {
    return this.form.controls.confirmacao;
  }

  async cadastrar(): Promise<void> {
    if (this.form.invalid) {
      this.form.markAllAsDirty();
      this.form.markAllAsTouched();
      return;
    }

    this.enviando.set(true);
    this.erro.set(null);

    try {
      const { nome, email, senha } = this.form.getRawValue();
      const resultado = await this.auth.cadastrar(nome.trim(), email.trim(), senha);

      if (!resultado.ok) {
        this.erro.set(resultado.mensagem);
        return;
      }

      await firstValueFrom(this.api.garantirPerfil()).catch((e) =>
        console.error('Não foi possível criar o perfil agora', e),
      );

      await this.router.navigateByUrl('/');
    } finally {
      this.enviando.set(false);
    }
  }
}
