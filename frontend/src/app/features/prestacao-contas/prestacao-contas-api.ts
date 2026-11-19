import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Pagina } from '../../shared/util/pagina';
import { TipoCaso } from '../casos/caso-api';

export type SituacaoPeriodo = 'AGUARDANDO_CURADOR' | 'EM_REVISAO' | 'PRONTO' | 'ENTREGUE';

export type TipoPendencia =
  | 'MOVIMENTACAO_PENDENTE'
  | 'LANCAMENTO_SEM_EXTRATO'
  | 'EXTRATO_FALTANDO'
  | 'SALDO_DIVERGENTE'
  | 'SALDO_INICIAL_DIFERENTE'
  | 'POSSIVEL_DUPLICIDADE';

export interface PrestacaoContasResumo {
  id: string;
  casoId: string;
  tipo: TipoCaso;
  numeroProcesso: string;
  pessoaProtegidaNome: string;
  casoEncerrado: boolean;
  dataInicio: string;
  dataFim: string;
  situacao: SituacaoPeriodo;
  prazoCurador: string | null;
  prazoEntrega: string;
  entregueEm: string | null;
  entregueNoPrazo: boolean | null;
  vencido: boolean;
}

export interface SaldoPeriodoResponse {
  contaBancariaId: string;
  contaDescricao: string;
  saldoInicial: number | null;
  saldoSugerido: number;
  saldoFinal: number | null;
}

export interface PendenciaResponse {
  tipo: TipoPendencia;
  referencia: string;
  texto: string;
  valor: number | null;
  justificativa: string | null;
}

export interface RelatorioResponse {
  id: string;
  versao: number;
  geradoEm: string;
  comRessalvas: boolean;
  quantidadePendencias: number;
  saldoFinalTotal: number;
  tamanhoBytes: number;
  hashSha256: string;
}

export interface PrestacaoContasDetalhe extends PrestacaoContasResumo {
  responsavelNome: string;
  notasExplicativas: string | null;
  saldos: SaldoPeriodoResponse[];
  pendencias: PendenciaResponse[];
  relatorios: RelatorioResponse[];
  relatorioEntregueId: string | null;
}

export interface PrestacaoContasFiltro {
  casoId: string | null;
  situacao: SituacaoPeriodo | null;
}

export interface SaldoInicialRequest {
  contaBancariaId: string;
  saldoInicial: number;
}

export interface JustificativaRequest {
  tipo: TipoPendencia;
  referencia: string;
  texto: string;
}

@Injectable({ providedIn: 'root' })
export class PrestacaoContasApi {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/prestacoes-contas`;

  listar(
    filtro: PrestacaoContasFiltro,
    pagina: number,
    tamanho: number,
  ): Observable<Pagina<PrestacaoContasResumo>> {
    const params: Record<string, string | number> = { pagina, tamanho };
    if (filtro.casoId) {
      params['casoId'] = filtro.casoId;
    }
    if (filtro.situacao) {
      params['situacao'] = filtro.situacao;
    }
    return this.http.get<Pagina<PrestacaoContasResumo>>(this.url, { params });
  }

  buscarPorId(id: string): Observable<PrestacaoContasDetalhe> {
    return this.http.get<PrestacaoContasDetalhe>(`${this.url}/${id}`);
  }

  atualizar(
    id: string,
    prazoCurador: string | null,
    notasExplicativas: string,
  ): Observable<PrestacaoContasDetalhe> {
    return this.http.put<PrestacaoContasDetalhe>(`${this.url}/${id}`, {
      prazoCurador,
      notasExplicativas,
    });
  }

  informarSaldosIniciais(id: string, saldos: SaldoInicialRequest[]): Observable<void> {
    return this.http.put<void>(`${this.url}/${id}/saldos-iniciais`, saldos);
  }

  salvarJustificativa(id: string, justificativa: JustificativaRequest): Observable<void> {
    return this.http.put<void>(`${this.url}/${id}/justificativas`, justificativa);
  }

  receberDoCurador(id: string): Observable<PrestacaoContasDetalhe> {
    return this.http.post<PrestacaoContasDetalhe>(`${this.url}/${id}/recebimento-curador`, null);
  }

  voltarParaRevisao(id: string): Observable<PrestacaoContasDetalhe> {
    return this.http.post<PrestacaoContasDetalhe>(`${this.url}/${id}/revisao`, null);
  }

  entregar(id: string, dataEntrega: string): Observable<PrestacaoContasDetalhe> {
    return this.http.post<PrestacaoContasDetalhe>(`${this.url}/${id}/entrega`, { dataEntrega });
  }

  gerarRelatorio(id: string): Observable<RelatorioResponse> {
    return this.http.post<RelatorioResponse>(`${this.url}/${id}/relatorios`, null);
  }

  baixarRelatorio(id: string, relatorioId: string): Observable<Blob> {
    return this.http.get(`${this.url}/${id}/relatorios/${relatorioId}/arquivo`, {
      responseType: 'blob',
    });
  }
}
