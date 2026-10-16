import { Component, inject, signal } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { Button } from 'primeng/button';
import { InputText } from 'primeng/inputtext';
import { Message } from 'primeng/message';
import { Password } from 'primeng/password';
import { AuthService } from '../../../core/auth/auth-service';
import { criarLoginForm } from '../forms/login.form';

@Component({
  imports: [ReactiveFormsModule, RouterLink, Button, InputText, Message, Password],
  selector: 'app-login',
  styleUrl: './login.scss',
  templateUrl: './login.html',
})
export class Login {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  readonly enviando = signal(false);
  readonly erro = signal<string | null>(null);

  readonly form = criarLoginForm();

  get email() {
    return this.form.controls.email;
  }

  get senha() {
    return this.form.controls.senha;
  }

  async entrar(): Promise<void> {
    if (this.form.invalid) {
      this.form.markAllAsDirty();
      this.form.markAllAsTouched();
      return;
    }

    this.enviando.set(true);
    this.erro.set(null);

    try {
      const { email, senha } = this.form.getRawValue();
      const resultado = await this.auth.entrar(email.trim(), senha);

      if (!resultado.ok) {
        this.erro.set(resultado.mensagem);
        return;
      }

      await this.router.navigateByUrl('/');
    } finally {
      this.enviando.set(false);
    }
  }
}
