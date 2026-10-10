import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface UsuarioResponse {
  id: string;
  nome: string;
  email: string;
}

@Injectable({ providedIn: 'root' })
export class UsuarioApi {
  private readonly http = inject(HttpClient);

  garantirPerfil(): Observable<UsuarioResponse> {
    return this.http.post<UsuarioResponse>(`${environment.apiUrl}/usuarios/me`, null);
  }
}
