import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Opcao, Pagina } from '../../shared/util/pagina';

export type TipoCaso = 'CURATELA' | 'TUTELA';
export type SituacaoCaso = 'ATIVO' | 'ENCERRADO';

export interface PeriodoPrestacaoResponse {
  id: string;
  dataInicio: string;
  dataFim: string;
  prazoEntrega: string;
}

export interface CasoResponse {
  id: string;
  tipo: TipoCaso;
  numeroProcesso: string;
  varaId: string;
  varaNome: string;
  comarcaId: string;
  comarcaNome: string;
  pessoaProtegidaId: string;
  pessoaProtegidaNome: string;
  responsavelId: string;
  responsavelNome: string;
  periodicidadeMeses: number;
  prazoEntregaDias: number;
  situacao: SituacaoCaso;
  encerradoEm: string | null;
  periodoAtual: PeriodoPrestacaoResponse | null;
}

export interface CasoRequest {
  tipo: TipoCaso;
  numeroProcesso: string;
  varaId: string;
  pessoaProtegidaId: string;
  responsavelId: string;
  periodicidadeMeses: number;
  prazoEntregaDias: number;
  inicioPeriodo: string;
}

export interface CasoFiltro {
  tipo: TipoCaso | null;
  comarcaId: string | null;
  situacao: SituacaoCaso | null;
  busca: string;
}

@Injectable({ providedIn: 'root' })
export class CasoApi {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/casos`;

  listar(filtro: CasoFiltro, pagina: number, tamanho: number): Observable<Pagina<CasoResponse>> {
    const params: Record<string, string | number> = { pagina, tamanho };
    if (filtro.tipo) {
      params['tipo'] = filtro.tipo;
    }
    if (filtro.comarcaId) {
      params['comarcaId'] = filtro.comarcaId;
    }
    if (filtro.situacao) {
      params['situacao'] = filtro.situacao;
    }
    if (filtro.busca.trim()) {
      params['busca'] = filtro.busca.trim();
    }
    return this.http.get<Pagina<CasoResponse>>(this.url, { params });
  }

  listarOpcoes(): Observable<Opcao[]> {
    return this.http.get<Opcao[]>(`${this.url}/opcoes`);
  }

  buscarPorId(id: string): Observable<CasoResponse> {
    return this.http.get<CasoResponse>(`${this.url}/${id}`);
  }

  criar(caso: CasoRequest): Observable<CasoResponse> {
    return this.http.post<CasoResponse>(this.url, caso);
  }

  atualizar(id: string, caso: CasoRequest): Observable<CasoResponse> {
    return this.http.put<CasoResponse>(`${this.url}/${id}`, caso);
  }

  encerrar(id: string, data: string): Observable<CasoResponse> {
    return this.http.put<CasoResponse>(`${this.url}/${id}/encerramento`, { data });
  }

  reabrir(id: string): Observable<CasoResponse> {
    return this.http.delete<CasoResponse>(`${this.url}/${id}/encerramento`);
  }

  excluir(id: string): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
