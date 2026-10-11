import { computed, inject, Injectable, signal } from '@angular/core';
import { Session } from '@supabase/supabase-js';
import { SUPABASE } from './supabase-client/supabase-client';

export type ResultadoAuth = { ok: true } | { ok: false; mensagem: string };

const MENSAGENS_CADASTRO: Record<string, string> = {
  user_already_exists: 'Este e-mail já está cadastrado.',
  email_exists: 'Este e-mail já está cadastrado.',
  weak_password: 'Senha fraca. Use ao menos 6 caracteres.',
  email_address_invalid: 'E-mail inválido.',
  over_request_rate_limit: 'Muitas tentativas. Aguarde um pouco e tente de novo.',
  signup_disabled: 'O cadastro está temporariamente indisponível.',
};
const MENSAGEM_PADRAO_CADASTRO = 'Não foi possível criar a conta. Tente novamente.';

const MENSAGENS_LOGIN: Record<string, string> = {
  invalid_credentials: 'E-mail ou senha inválidos.',
  email_not_confirmed: 'Confirme seu e-mail antes de entrar.',
  user_banned: 'Esta conta está bloqueada.',
  over_request_rate_limit: 'Muitas tentativas. Aguarde um pouco e tente de novo.',
};
const MENSAGEM_PADRAO_LOGIN = 'Não foi possível entrar. Tente novamente.';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly supabase = inject(SUPABASE);

  private readonly _sessao = signal<Session | null>(null);

  readonly sessao = this._sessao.asReadonly();
  readonly usuario = computed(() => this._sessao()?.user ?? null);
  readonly autenticado = computed(() => this._sessao() !== null);

  async iniciar(): Promise<void> {
    const { data } = await this.supabase.auth.getSession();
    this._sessao.set(data.session);

    this.supabase.auth.onAuthStateChange((_evento, sessao) => {
      this._sessao.set(sessao);
    });
  }

  async cadastrar(nome: string, email: string, senha: string): Promise<ResultadoAuth> {
    const { error } = await this.supabase.auth.signUp({
      email,
      password: senha,
      options: { data: { nome } },
    });

    if (error) {
      console.error('[auth] signUp falhou', error.code, error.status, error.message);
      return {
        ok: false,
        mensagem: MENSAGENS_CADASTRO[error.code ?? ''] ?? MENSAGEM_PADRAO_CADASTRO,
      };
    }
    return { ok: true };
  }

  async entrar(email: string, senha: string): Promise<ResultadoAuth> {
    const { data, error } = await this.supabase.auth.signInWithPassword({
      email,
      password: senha,
    });

    if (error) {
      console.error('[auth] signIn falhou', error.code, error.status, error.message);
      return { ok: false, mensagem: MENSAGENS_LOGIN[error.code ?? ''] ?? MENSAGEM_PADRAO_LOGIN };
    }

    this._sessao.set(data.session);
    return { ok: true };
  }

  async sair(): Promise<void> {
    await this.supabase.auth.signOut();
    this._sessao.set(null);
  }

  async obterAccessToken(): Promise<string | null> {
    const { data } = await this.supabase.auth.getSession();
    return data.session?.access_token ?? null;
  }
}
