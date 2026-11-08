import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Pagina } from '../../shared/util/pagina';
import { TipoCategoria } from '../categorias/categoria-api';

export type SituacaoLancamento = 'PENDENTE' | 'CONCILIADO';

export interface LancamentoResponse {
  id: string;
  contaBancariaId: string;
  categoriaId: string;
  categoriaNome: string;
  tipo: TipoCategoria;
  dataLancamento: string;
  valor: number;
  descricao: string;
  situacao: SituacaoLancamento;
}

export interface LancamentoRequest {
  contaBancariaId: string;
  categoriaId: string;
  tipo: TipoCategoria;
  dataLancamento: string;
  valor: number;
  descricao: string;
}

export type LancamentoUpdateRequest = Omit<LancamentoRequest, 'tipo'> & { justificativa: string };

export interface LancamentoFiltro {
  dataInicio: string | null;
  dataFim: string | null;
  contaBancariaId: string | null;
  tipo: TipoCategoria | null;
  categoriaId: string | null;
  situacao: SituacaoLancamento | null;
}

@Injectable({ providedIn: 'root' })
export class LancamentoApi {
  private readonly http = inject(HttpClient);

  listar(
    casoId: string,
    filtro: LancamentoFiltro,
    pagina: number,
    tamanho: number,
  ): Observable<Pagina<LancamentoResponse>> {
    const params: Record<string, string | number> = { pagina, tamanho };
    if (filtro.dataInicio) {
      params['dataInicio'] = filtro.dataInicio;
    }
    if (filtro.dataFim) {
      params['dataFim'] = filtro.dataFim;
    }
    if (filtro.contaBancariaId) {
      params['contaBancariaId'] = filtro.contaBancariaId;
    }
    if (filtro.tipo) {
      params['tipo'] = filtro.tipo;
    }
    if (filtro.categoriaId) {
      params['categoriaId'] = filtro.categoriaId;
    }
    if (filtro.situacao) {
      params['situacao'] = filtro.situacao;
    }
    return this.http.get<Pagina<LancamentoResponse>>(this.url(casoId), { params });
  }

  criar(casoId: string, lancamento: LancamentoRequest): Observable<LancamentoResponse> {
    return this.http.post<LancamentoResponse>(this.url(casoId), lancamento);
  }

  atualizar(
    casoId: string,
    id: string,
    lancamento: LancamentoUpdateRequest,
  ): Observable<LancamentoResponse> {
    return this.http.put<LancamentoResponse>(`${this.url(casoId)}/${id}`, lancamento);
  }

  excluir(casoId: string, id: string, justificativa: string): Observable<void> {
    return this.http.delete<void>(`${this.url(casoId)}/${id}`, { body: { justificativa } });
  }

  private url(casoId: string): string {
    return `${environment.apiUrl}/casos/${casoId}/lancamentos`;
  }
}
