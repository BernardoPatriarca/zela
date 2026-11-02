import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Opcao, Pagina } from '../../shared/util/pagina';

export interface ComarcaResponse {
  id: string;
  nome: string;
  uf: string;
  sistema: boolean;
}

export type ComarcaRequest = Pick<ComarcaResponse, 'nome' | 'uf'>;

@Injectable({ providedIn: 'root' })
export class ComarcaApi {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/comarcas`;

  listar(busca: string, pagina: number, tamanho: number): Observable<Pagina<ComarcaResponse>> {
    const params: Record<string, string | number> = { pagina, tamanho };
    if (busca.trim()) {
      params['busca'] = busca.trim();
    }
    return this.http.get<Pagina<ComarcaResponse>>(this.url, { params });
  }

  listarOpcoes(): Observable<Opcao[]> {
    return this.http.get<Opcao[]>(`${this.url}/opcoes`);
  }

  criar(comarca: ComarcaRequest): Observable<ComarcaResponse> {
    return this.http.post<ComarcaResponse>(this.url, comarca);
  }

  atualizar(id: string, comarca: ComarcaRequest): Observable<ComarcaResponse> {
    return this.http.put<ComarcaResponse>(`${this.url}/${id}`, comarca);
  }

  excluir(id: string): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
