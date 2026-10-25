import { HttpErrorResponse } from '@angular/common/http';

const MENSAGEM_PADRAO = 'Não foi possível concluir a operação. Tente novamente.';

export function mensagemDeErro(e: unknown): string {
  if (e instanceof HttpErrorResponse) {
    return e.error?.fields?.[0]?.message ?? e.error?.message ?? MENSAGEM_PADRAO;
  }
  return MENSAGEM_PADRAO;
}
