import { inject, Injectable } from '@angular/core';
import { SUPABASE } from './supabase-client/supabase-client';

export type ResultadoCadastro = { ok: true } | { ok: false; mensagem: string };

const MENSAGENS: Record<string, string> = {
  user_already_exists: 'Este e-mail já está cadastrado.',
  email_exists: 'Este e-mail já está cadastrado.',
  weak_password: 'Senha fraca. Use ao menos 6 caracteres.',
  email_address_invalid: 'E-mail inválido.',
  over_request_rate_limit: 'Muitas tentativas. Aguarde um pouco e tente de novo.',
  signup_disabled: 'O cadastro está temporariamente indisponível.',
};
const MENSAGEM_PADRAO = 'Não foi possível criar a conta. Tente novamente.';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly supabase = inject(SUPABASE);

  async cadastrar(nome: string, email: string, senha: string): Promise<ResultadoCadastro> {
    const { error } = await this.supabase.auth.signUp({
      email,
      password: senha,
      options: { data: { nome } },
    });

    if (error) {
      console.error('[auth] signUp falhou', error.code, error.status, error.message);
      return { ok: false, mensagem: MENSAGENS[error.code ?? ''] ?? MENSAGEM_PADRAO };
    }
    return { ok: true };
  }

  async obterAccessToken(): Promise<string | null> {
    const { data } = await this.supabase.auth.getSession();
    return data.session?.access_token ?? null;
  }
}
