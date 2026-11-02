import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Opcao, Pagina } from '../../shared/util/pagina';

export interface PessoaResponse {
  id: string;
  nome: string;
  cpf: string | null;
  dataNascimento: string;
  endereco: string | null;
  observacao: string | null;
}

export type PessoaRequest = Omit<PessoaResponse, 'id'>;

@Injectable({ providedIn: 'root' })
export class PessoaApi {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/pessoas`;

  listar(busca: string, pagina: number, tamanho: number): Observable<Pagina<PessoaResponse>> {
    const params: Record<string, string | number> = { pagina, tamanho };
    if (busca.trim()) {
      params['busca'] = busca.trim();
    }
    return this.http.get<Pagina<PessoaResponse>>(this.url, { params });
  }

  listarOpcoes(): Observable<Opcao[]> {
    return this.http.get<Opcao[]>(`${this.url}/opcoes`);
  }

  criar(pessoa: PessoaRequest): Observable<PessoaResponse> {
    return this.http.post<PessoaResponse>(this.url, pessoa);
  }

  atualizar(id: string, pessoa: PessoaRequest): Observable<PessoaResponse> {
    return this.http.put<PessoaResponse>(`${this.url}/${id}`, pessoa);
  }

  excluir(id: string): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
