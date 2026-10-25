import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

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

  listar(busca = ''): Observable<PessoaResponse[]> {
    const params: Record<string, string> = busca.trim() ? { busca: busca.trim() } : {};
    return this.http.get<PessoaResponse[]>(this.url, { params });
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
