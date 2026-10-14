import { HttpErrorResponse, HttpInterceptorFn, HttpRequest } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, from, switchMap, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthService } from './auth-service';

function ehRequisicaoDaApi(req: HttpRequest<unknown>): boolean {
  const base = environment.apiUrl;
  return !!base && (req.url === base || req.url.startsWith(`${base}/`));
}

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  if (!ehRequisicaoDaApi(req)) {
    return next(req);
  }

  const auth = inject(AuthService);
  const router = inject(Router);

  return from(auth.obterAccessToken()).pipe(
    switchMap((token) =>
      next(token ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : req),
    ),
    catchError((erro: HttpErrorResponse) => {
      if (erro.status === 401 && auth.autenticado()) {
        console.error('[auth] a API recusou a sessão', req.method, req.url);
        void auth.sair().then(() => router.navigateByUrl('/login'));
      }
      return throwError(() => erro);
    }),
  );
};
