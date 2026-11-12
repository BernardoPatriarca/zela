import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Opcao, Pagina } from '../../shared/util/pagina';

export interface BancoResponse {
  id: string;
  codigo: string;
  nome: string;
  sistema: boolean;
}

export type BancoRequest = Pick<BancoResponse, 'codigo' | 'nome'>;

@Injectable({ providedIn: 'root' })
export class BancoApi {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/bancos`;

  listar(busca: string, pagina: number, tamanho: number): Observable<Pagina<BancoResponse>> {
    const params: Record<string, string | number> = { pagina, tamanho };
    if (busca.trim()) {
      params['busca'] = busca.trim();
    }
    return this.http.get<Pagina<BancoResponse>>(this.url, { params });
  }

  listarOpcoes(): Observable<Opcao[]> {
    return this.http.get<Opcao[]>(`${this.url}/opcoes`);
  }

  criar(banco: BancoRequest): Observable<BancoResponse> {
    return this.http.post<BancoResponse>(this.url, banco);
  }

  atualizar(id: string, banco: BancoRequest): Observable<BancoResponse> {
    return this.http.put<BancoResponse>(`${this.url}/${id}`, banco);
  }

  excluir(id: string): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
