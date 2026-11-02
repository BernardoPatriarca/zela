import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Opcao, Pagina } from '../../shared/util/pagina';

export interface VaraResponse {
  id: string;
  nome: string;
  comarcaId: string;
  comarcaNome: string;
  comarcaUf: string;
  sistema: boolean;
}

export type VaraRequest = Pick<VaraResponse, 'comarcaId' | 'nome'>;

@Injectable({ providedIn: 'root' })
export class VaraApi {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/varas`;

  listar(
    comarcaId: string | null,
    busca: string,
    pagina: number,
    tamanho: number,
  ): Observable<Pagina<VaraResponse>> {
    const params: Record<string, string | number> = { pagina, tamanho };
    if (comarcaId) {
      params['comarcaId'] = comarcaId;
    }
    if (busca.trim()) {
      params['busca'] = busca.trim();
    }
    return this.http.get<Pagina<VaraResponse>>(this.url, { params });
  }

  listarOpcoes(comarcaId: string): Observable<Opcao[]> {
    return this.http.get<Opcao[]>(`${this.url}/opcoes`, { params: { comarcaId } });
  }

  criar(vara: VaraRequest): Observable<VaraResponse> {
    return this.http.post<VaraResponse>(this.url, vara);
  }

  atualizar(id: string, vara: VaraRequest): Observable<VaraResponse> {
    return this.http.put<VaraResponse>(`${this.url}/${id}`, vara);
  }

  excluir(id: string): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
