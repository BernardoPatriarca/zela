import { HttpErrorResponse } from '@angular/common/http';
import { DatePipe } from '@angular/common';
import { Component, effect, inject, input, signal, untracked } from '@angular/core';
import { FormControl, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { MessageService } from 'primeng/api';
import { Button } from 'primeng/button';
import { DatePicker } from 'primeng/datepicker';
import { Dialog } from 'primeng/dialog';
import { FileSelectEvent, FileUpload } from 'primeng/fileupload';
import { Message } from 'primeng/message';
import { Select } from 'primeng/select';
import { TableLazyLoadEvent, TableModule } from 'primeng/table';
import { Textarea } from 'primeng/textarea';
import { Toast } from 'primeng/toast';
import {
  ImportacaoExtratoApi,
  ImportacaoExtratoFiltro,
  ImportacaoExtratoResponse,
  ImportacaoExtratoResultado,
} from '../importacao-extrato-api';
import { CasoApi, CasoResponse } from '../../casos/caso-api';
import { ContaBancariaApi } from '../../contas-bancarias/conta-bancaria-api';
import { MoedaPipe } from '../../../shared/pipes/moeda-pipe';
import { formatLocalDate } from '../../../shared/util/data';
import { mensagemDeErro } from '../../../shared/util/erro';
import { inicioDaUltimaPagina, ITENS_POR_PAGINA, Opcao } from '../../../shared/util/pagina';

const TAMANHO_MAXIMO_MB = 4;

interface TransacaoInvalida {
  field: string;
  message: string;
}

@Component({
  imports: [
    FormsModule,
    ReactiveFormsModule,
    DatePipe,
    MoedaPipe,
    Button,
    DatePicker,
    Dialog,
    FileUpload,
    Message,
    Select,
    TableModule,
    Textarea,
    Toast,
  ],
  providers: [MessageService],
  selector: 'app-importacoes-extrato-page',
  templateUrl: './importacoes-extrato-page.html',
})
export class ImportacoesExtratoPage {
  private readonly api = inject(ImportacaoExtratoApi);
  private readonly casoApi = inject(CasoApi);
  private readonly contaApi = inject(ContaBancariaApi);
  private readonly router = inject(Router);
  private readonly rota = inject(ActivatedRoute);
  private readonly mensagens = inject(MessageService);

  readonly casoId = input<string>();

  readonly casos = signal<Opcao[]>([]);
  readonly caso = signal<CasoResponse | null>(null);
  readonly contas = signal<Opcao[]>([]);
  readonly importacoes = signal<ImportacaoExtratoResponse[]>([]);
  readonly total = signal(0);
  readonly primeiro = signal(0);
  readonly linhas = ITENS_POR_PAGINA;
  readonly carregando = signal(false);
  readonly erroLista = signal(false);

  readonly dialogAberto = signal(false);
  readonly importando = signal(false);
  readonly erro = signal<string | null>(null);
  readonly transacoesInvalidas = signal<TransacaoInvalida[]>([]);
  readonly arquivo = signal<File | null>(null);
  readonly resultado = signal<ImportacaoExtratoResultado | null>(null);
  readonly contaImportacao = new FormControl<string | null>(null, Validators.required);
  readonly tamanhoMaximoMb = TAMANHO_MAXIMO_MB;

  readonly importacaoExcluindo = signal<ImportacaoExtratoResponse | null>(null);
  readonly excluindo = signal(false);
  readonly erroExclusao = signal<string | null>(null);
  readonly justificativaExclusao = new FormControl('', {
    nonNullable: true,
    validators: [Validators.required, Validators.minLength(10), Validators.maxLength(500)],
  });

  filtro: ImportacaoExtratoFiltro = this.filtroVazio();
  periodo: Date[] | null = null;
  private ultimaRequisicao = 0;

  constructor() {
    void this.carregarCasos();
    effect(() => {
      const casoId = this.casoId();
      untracked(() => {
        this.primeiro.set(0);
        this.caso.set(null);
        this.contas.set([]);
        this.importacoes.set([]);
        this.total.set(0);
        this.filtro = this.filtroVazio();
        this.periodo = null;
        if (casoId) {
          void this.carregarCaso();
        }
      });
    });
  }

  async carregarCaso(): Promise<void> {
    const casoId = this.casoId();
    if (!casoId) {
      return;
    }

    const requisicao = ++this.ultimaRequisicao;
    this.carregando.set(true);
    this.erroLista.set(false);

    try {
      const [caso, contas] = await Promise.all([
        firstValueFrom(this.casoApi.buscarPorId(casoId)),
        firstValueFrom(this.contaApi.listar(casoId, 0, 100)),
      ]);
      if (requisicao !== this.ultimaRequisicao) {
        return;
      }
      this.caso.set(caso);
      this.contas.set(
        contas.itens.map((conta) => ({
          id: conta.id,
          nome: `${conta.bancoCodigo} - ${conta.bancoNome} · ${conta.agencia} / ${conta.numero}`,
        })),
      );
      this.aplicarPeriodoPadrao();
    } catch (e) {
      if (requisicao === this.ultimaRequisicao) {
        console.error('[importacoes-extrato] falha ao carregar o caso', e);
        this.erroLista.set(true);
        this.carregando.set(false);
      }
      return;
    }

    await this.carregar();
  }

  async carregar(): Promise<void> {
    const casoId = this.casoId();
    if (!casoId) {
      return;
    }

    const requisicao = ++this.ultimaRequisicao;
    this.carregando.set(true);
    this.erroLista.set(false);

    try {
      const pagina = await firstValueFrom(
        this.api.listar(casoId, this.filtro, this.primeiro() / this.linhas, this.linhas),
      );
      if (requisicao === this.ultimaRequisicao) {
        if (pagina.itens.length === 0 && this.primeiro() > 0) {
          this.primeiro.set(inicioDaUltimaPagina(pagina.total, this.linhas));
          void this.carregar();
          return;
        }
        this.importacoes.set(pagina.itens);
        this.total.set(pagina.total);
      }
    } catch (e) {
      if (requisicao === this.ultimaRequisicao) {
        console.error('[importacoes-extrato] falha ao carregar as importações', e);
        this.erroLista.set(true);
      }
    } finally {
      if (requisicao === this.ultimaRequisicao) {
        this.carregando.set(false);
      }
    }
  }

  async carregarCasos(): Promise<void> {
    try {
      this.casos.set(await firstValueFrom(this.casoApi.listarOpcoes()));
    } catch (e) {
      console.error('[importacoes-extrato] falha ao carregar os casos', e);
    }
  }

  tentarNovamente(): void {
    if (this.caso()) {
      void this.carregar();
    } else {
      void this.carregarCaso();
    }
  }

  selecionarCaso(casoId: string | null): void {
    void this.router.navigate([], { relativeTo: this.rota, queryParams: { casoId } });
  }

  paginar(evento: TableLazyLoadEvent): void {
    this.primeiro.set(evento.first ?? 0);
    void this.carregar();
  }

  filtrar(): void {
    this.primeiro.set(0);
    void this.carregar();
  }

  filtrarPorPeriodo(periodo: Date[] | null): void {
    this.periodo = periodo;
    if (periodo?.[0] && !periodo[1]) {
      return;
    }
    this.filtro.dataInicio = periodo?.[0] ? formatLocalDate(periodo[0]) : null;
    this.filtro.dataFim = periodo?.[1] ? formatLocalDate(periodo[1]) : null;
    this.filtrar();
  }

  nomeConta(contaBancariaId: string): string {
    return this.contas().find((conta) => conta.id === contaBancariaId)?.nome ?? '-';
  }

  abrirImportacao(): void {
    this.erro.set(null);
    this.transacoesInvalidas.set([]);
    this.arquivo.set(null);
    this.resultado.set(null);
    this.contaImportacao.reset(this.filtro.contaBancariaId);
    this.dialogAberto.set(true);
  }

  selecionarArquivo(evento: FileSelectEvent): void {
    this.erro.set(null);
    this.transacoesInvalidas.set([]);
    const arquivo = evento.currentFiles[0] ?? null;
    if (arquivo && !arquivo.name.toLowerCase().endsWith('.ofx')) {
      this.arquivo.set(null);
      this.erro.set('Selecione um arquivo com a extensão .ofx.');
      return;
    }
    if (arquivo && arquivo.size > TAMANHO_MAXIMO_MB * 1024 * 1024) {
      this.arquivo.set(null);
      this.erro.set(`O arquivo deve ter no máximo ${TAMANHO_MAXIMO_MB} MB.`);
      return;
    }
    this.arquivo.set(arquivo);
  }

  async importar(): Promise<void> {
    const arquivo = this.arquivo();
    if (this.contaImportacao.invalid || !arquivo) {
      this.contaImportacao.markAsTouched();
      this.erro.set(arquivo ? null : 'Selecione o arquivo OFX do extrato.');
      return;
    }

    this.importando.set(true);
    this.erro.set(null);
    this.transacoesInvalidas.set([]);

    try {
      this.resultado.set(
        await firstValueFrom(
          this.api.importar(this.casoId()!, this.contaImportacao.value!, arquivo),
        ),
      );
      await this.carregar();
    } catch (e) {
      if (e instanceof HttpErrorResponse && e.status === 422 && e.error?.fields?.length) {
        this.erro.set(e.error.message);
        this.transacoesInvalidas.set(e.error.fields);
      } else {
        this.erro.set(mensagemDeErro(e));
      }
    } finally {
      this.importando.set(false);
    }
  }

  abrirExclusao(importacao: ImportacaoExtratoResponse): void {
    this.erroExclusao.set(null);
    this.justificativaExclusao.reset('');
    this.importacaoExcluindo.set(importacao);
  }

  fecharExclusao(): void {
    this.importacaoExcluindo.set(null);
  }

  async excluir(): Promise<void> {
    const importacao = this.importacaoExcluindo();
    if (!importacao || this.justificativaExclusao.invalid) {
      this.justificativaExclusao.markAsTouched();
      return;
    }

    this.excluindo.set(true);
    this.erroExclusao.set(null);

    try {
      await firstValueFrom(
        this.api.excluir(this.casoId()!, importacao.id, this.justificativaExclusao.value.trim()),
      );
      this.importacaoExcluindo.set(null);
      this.mensagens.add({ severity: 'success', summary: 'Importação excluída.' });
      await this.carregar();
    } catch (e) {
      this.erroExclusao.set(mensagemDeErro(e));
    } finally {
      this.excluindo.set(false);
    }
  }

  private aplicarPeriodoPadrao(): void {
    const fim = new Date();
    const inicio = new Date(fim.getFullYear(), fim.getMonth() - 3, fim.getDate());
    this.periodo = [inicio, fim];
    this.filtro.dataInicio = formatLocalDate(inicio);
    this.filtro.dataFim = formatLocalDate(fim);
  }

  private filtroVazio(): ImportacaoExtratoFiltro {
    return {
      dataInicio: null,
      dataFim: null,
      contaBancariaId: null,
    };
  }
}
