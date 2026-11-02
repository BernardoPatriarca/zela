import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Pagina } from '../../shared/util/pagina';

export type Banco =
  | 'BANCO_DO_BRASIL'
  | 'BANRISUL'
  | 'BRADESCO'
  | 'C6'
  | 'CAIXA'
  | 'CRESOL'
  | 'INTER'
  | 'ITAU'
  | 'MERCADO_PAGO'
  | 'NUBANK'
  | 'SANTANDER'
  | 'SICOOB'
  | 'SICREDI'
  | 'OUTRO';

export type TipoConta = 'CORRENTE' | 'POUPANCA' | 'PAGAMENTO';

export interface ContaBancariaResponse {
  id: string;
  casoId: string;
  banco: Banco;
  tipo: TipoConta;
  agencia: string;
  numero: string;
  descricao: string | null;
  saldoInicial: number;
  dataSaldoInicial: string;
}

export type ContaBancariaRequest = Omit<ContaBancariaResponse, 'id' | 'casoId'>;

@Injectable({ providedIn: 'root' })
export class ContaBancariaApi {
  private readonly http = inject(HttpClient);

  listar(
    casoId: string,
    pagina: number,
    tamanho: number,
  ): Observable<Pagina<ContaBancariaResponse>> {
    return this.http.get<Pagina<ContaBancariaResponse>>(this.url(casoId), {
      params: { pagina, tamanho },
    });
  }

  criar(casoId: string, conta: ContaBancariaRequest): Observable<ContaBancariaResponse> {
    return this.http.post<ContaBancariaResponse>(this.url(casoId), conta);
  }

  atualizar(
    casoId: string,
    id: string,
    conta: ContaBancariaRequest,
  ): Observable<ContaBancariaResponse> {
    return this.http.put<ContaBancariaResponse>(`${this.url(casoId)}/${id}`, conta);
  }

  excluir(casoId: string, id: string): Observable<void> {
    return this.http.delete<void>(`${this.url(casoId)}/${id}`);
  }

  private url(casoId: string): string {
    return `${environment.apiUrl}/casos/${casoId}/contas-bancarias`;
  }
}
