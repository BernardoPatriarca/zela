import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Pagina } from '../../shared/util/pagina';
import { TipoCategoria } from '../categorias/categoria-api';

export type SituacaoMovimentacao = 'PENDENTE' | 'CONCILIADA' | 'IGNORADA';
export type FormaConciliacao = 'AUTOMATICA' | 'MANUAL';

export interface MovimentacaoConciliacaoResponse {
  id: string;
  contaBancariaId: string;
  dataMovimentacao: string;
  descricao: string;
  tipo: TipoCategoria;
  valor: number;
  situacao: SituacaoMovimentacao;
  formaConciliacao: FormaConciliacao | null;
  lancamentoId: string | null;
  lancamentoDescricao: string | null;
  lancamentoCategoriaNome: string | null;
  candidatos: number;
}

export interface ConciliacaoResumo {
  pendentes: number;
  conciliadas: number;
  ignoradas: number;
}

export interface ConciliacaoAutomaticaResultado {
  conciliadas: number;
  pendentes: number;
  ambiguas: number;
}

export interface ConciliacaoFiltro {
  dataInicio: string | null;
  dataFim: string | null;
  contaBancariaId: string | null;
  situacao: SituacaoMovimentacao | null;
}

@Injectable({ providedIn: 'root' })
export class ConciliacaoApi {
  private readonly http = inject(HttpClient);

  listar(
    casoId: string,
    filtro: ConciliacaoFiltro,
    pagina: number,
    tamanho: number,
  ): Observable<Pagina<MovimentacaoConciliacaoResponse>> {
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
    if (filtro.situacao) {
      params['situacao'] = filtro.situacao;
    }
    return this.http.get<Pagina<MovimentacaoConciliacaoResponse>>(
      `${this.url(casoId)}/movimentacoes`,
      { params },
    );
  }

  resumo(casoId: string, filtro: ConciliacaoFiltro): Observable<ConciliacaoResumo> {
    const params: Record<string, string> = {};
    if (filtro.dataInicio) {
      params['dataInicio'] = filtro.dataInicio;
    }
    if (filtro.dataFim) {
      params['dataFim'] = filtro.dataFim;
    }
    if (filtro.contaBancariaId) {
      params['contaBancariaId'] = filtro.contaBancariaId;
    }
    return this.http.get<ConciliacaoResumo>(`${this.url(casoId)}/resumo`, { params });
  }

  conciliarAutomaticamente(
    casoId: string,
    contaBancariaId: string | null,
  ): Observable<ConciliacaoAutomaticaResultado> {
    const params: Record<string, string> = {};
    if (contaBancariaId) {
      params['contaBancariaId'] = contaBancariaId;
    }
    return this.http.post<ConciliacaoAutomaticaResultado>(`${this.url(casoId)}/automatica`, null, {
      params,
    });
  }

  private url(casoId: string): string {
    return `${environment.apiUrl}/casos/${casoId}/conciliacao`;
  }
}
