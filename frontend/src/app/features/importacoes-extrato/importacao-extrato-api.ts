import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Pagina } from '../../shared/util/pagina';

export interface ImportacaoExtratoResponse {
  id: string;
  contaBancariaId: string;
  nomeArquivo: string;
  tamanhoBytes: number;
  dataInicio: string;
  dataFim: string;
  saldoFinal: number | null;
  dataSaldoFinal: string | null;
  total: number;
  importadas: number;
  duplicadas: number;
  ignoradas: number;
  criadoEm: string;
}

export interface ImportacaoExtratoResultado {
  importacao: ImportacaoExtratoResponse;
  conciliadasAutomaticamente: number;
  avisos: string[];
}

export interface ImportacaoExtratoFiltro {
  dataInicio: string | null;
  dataFim: string | null;
  contaBancariaId: string | null;
}

@Injectable({ providedIn: 'root' })
export class ImportacaoExtratoApi {
  private readonly http = inject(HttpClient);

  listar(
    casoId: string,
    filtro: ImportacaoExtratoFiltro,
    pagina: number,
    tamanho: number,
  ): Observable<Pagina<ImportacaoExtratoResponse>> {
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
    return this.http.get<Pagina<ImportacaoExtratoResponse>>(this.url(casoId), { params });
  }

  importar(
    casoId: string,
    contaBancariaId: string,
    arquivo: File,
  ): Observable<ImportacaoExtratoResultado> {
    const dados = new FormData();
    dados.append('contaBancariaId', contaBancariaId);
    dados.append('arquivo', arquivo, arquivo.name);
    return this.http.post<ImportacaoExtratoResultado>(this.url(casoId), dados);
  }

  excluir(casoId: string, id: string, justificativa: string): Observable<void> {
    return this.http.delete<void>(`${this.url(casoId)}/${id}`, { body: { justificativa } });
  }

  private url(casoId: string): string {
    return `${environment.apiUrl}/casos/${casoId}/importacoes-extrato`;
  }
}
