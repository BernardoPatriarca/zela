import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Opcao, Pagina } from '../../shared/util/pagina';

export type TipoCategoria = 'RECEITA' | 'DESPESA';

export interface CategoriaResponse {
  id: string;
  nome: string;
  tipo: TipoCategoria;
  sistema: boolean;
}

export type CategoriaRequest = Pick<CategoriaResponse, 'nome' | 'tipo'>;

export type CategoriaUpdateRequest = Pick<CategoriaResponse, 'nome'>;

@Injectable({ providedIn: 'root' })
export class CategoriaApi {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/categorias`;

  listar(
    tipo: TipoCategoria | null,
    busca: string,
    pagina: number,
    tamanho: number,
  ): Observable<Pagina<CategoriaResponse>> {
    const params: Record<string, string | number> = { pagina, tamanho };
    if (tipo) {
      params['tipo'] = tipo;
    }
    if (busca.trim()) {
      params['busca'] = busca.trim();
    }
    return this.http.get<Pagina<CategoriaResponse>>(this.url, { params });
  }

  listarOpcoes(tipo: TipoCategoria): Observable<Opcao[]> {
    return this.http.get<Opcao[]>(`${this.url}/opcoes`, { params: { tipo } });
  }

  criar(categoria: CategoriaRequest): Observable<CategoriaResponse> {
    return this.http.post<CategoriaResponse>(this.url, categoria);
  }

  atualizar(id: string, categoria: CategoriaUpdateRequest): Observable<CategoriaResponse> {
    return this.http.put<CategoriaResponse>(`${this.url}/${id}`, categoria);
  }

  excluir(id: string): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
