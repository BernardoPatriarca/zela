import { DatePipe } from '@angular/common';
import { Component, effect, inject, input, signal, untracked } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { ConfirmationService, MessageService } from 'primeng/api';
import { Button } from 'primeng/button';
import { ConfirmDialog } from 'primeng/confirmdialog';
import { DatePicker } from 'primeng/datepicker';
import { Message } from 'primeng/message';
import { Select } from 'primeng/select';
import { TableLazyLoadEvent, TableModule } from 'primeng/table';
import { Tag } from 'primeng/tag';
import { Toast } from 'primeng/toast';
import {
  ConciliacaoApi,
  ConciliacaoFiltro,
  ConciliacaoResumo,
  FormaConciliacao,
  MovimentacaoConciliacaoResponse,
  SituacaoMovimentacao,
} from '../conciliacao-api';
import { CasoApi, CasoResponse } from '../../casos/caso-api';
import { TipoCategoria } from '../../categorias/categoria-api';
import { ContaBancariaApi } from '../../contas-bancarias/conta-bancaria-api';
import { MoedaPipe } from '../../../shared/pipes/moeda-pipe';
import { formatLocalDate, parseLocalDate } from '../../../shared/util/data';
import { mensagemDeErro } from '../../../shared/util/erro';
import { inicioDaUltimaPagina, ITENS_POR_PAGINA, Opcao } from '../../../shared/util/pagina';

const SITUACOES: { label: string; value: SituacaoMovimentacao }[] = [
  { label: 'Pendente', value: 'PENDENTE' },
  { label: 'Conciliada', value: 'CONCILIADA' },
  { label: 'Ignorada', value: 'IGNORADA' },
];

const FORMAS: { label: string; value: FormaConciliacao }[] = [
  { label: 'Automática', value: 'AUTOMATICA' },
  { label: 'Manual', value: 'MANUAL' },
];

@Component({
  imports: [
    FormsModule,
    DatePipe,
    MoedaPipe,
    Button,
    ConfirmDialog,
    DatePicker,
    Message,
    Select,
    TableModule,
    Tag,
    Toast,
  ],
  providers: [ConfirmationService, MessageService],
  selector: 'app-conciliacao-page',
  templateUrl: './conciliacao-page.html',
})
export class ConciliacaoPage {
  private readonly api = inject(ConciliacaoApi);
  private readonly casoApi = inject(CasoApi);
  private readonly contaApi = inject(ContaBancariaApi);
  private readonly router = inject(Router);
  private readonly rota = inject(ActivatedRoute);
  private readonly confirmacao = inject(ConfirmationService);
  private readonly mensagens = inject(MessageService);

  readonly casoId = input<string>();

  readonly casos = signal<Opcao[]>([]);
  readonly caso = signal<CasoResponse | null>(null);
  readonly contas = signal<Opcao[]>([]);
  readonly movimentacoes = signal<MovimentacaoConciliacaoResponse[]>([]);
  readonly resumo = signal<ConciliacaoResumo | null>(null);
  readonly total = signal(0);
  readonly primeiro = signal(0);
  readonly linhas = ITENS_POR_PAGINA;
  readonly carregando = signal(false);
  readonly erroLista = signal(false);
  readonly conciliando = signal(false);

  readonly situacoes = SITUACOES;

  filtro: ConciliacaoFiltro = this.filtroVazio();
  periodo: Date[] | null = null;
  private ultimaRequisicao = 0;
  private ultimaRequisicaoResumo = 0;

  constructor() {
    void this.carregarCasos();
    effect(() => {
      const casoId = this.casoId();
      untracked(() => {
        this.primeiro.set(0);
        this.caso.set(null);
        this.contas.set([]);
        this.movimentacoes.set([]);
        this.resumo.set(null);
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
      this.aplicarPeriodoPadrao(caso);
    } catch (e) {
      if (requisicao === this.ultimaRequisicao) {
        console.error('[conciliacao] falha ao carregar o caso', e);
        this.erroLista.set(true);
        this.carregando.set(false);
      }
      return;
    }

    await Promise.all([this.carregar(), this.carregarResumo()]);
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
        this.movimentacoes.set(pagina.itens);
        this.total.set(pagina.total);
      }
    } catch (e) {
      if (requisicao === this.ultimaRequisicao) {
        console.error('[conciliacao] falha ao carregar as movimentações', e);
        this.erroLista.set(true);
      }
    } finally {
      if (requisicao === this.ultimaRequisicao) {
        this.carregando.set(false);
      }
    }
  }

  async carregarResumo(): Promise<void> {
    const casoId = this.casoId();
    if (!casoId) {
      return;
    }

    const requisicao = ++this.ultimaRequisicaoResumo;

    try {
      const resumo = await firstValueFrom(this.api.resumo(casoId, this.filtro));
      if (requisicao === this.ultimaRequisicaoResumo) {
        this.resumo.set(resumo);
      }
    } catch (e) {
      if (requisicao === this.ultimaRequisicaoResumo) {
        console.error('[conciliacao] falha ao carregar o resumo', e);
        this.resumo.set(null);
      }
    }
  }

  async carregarCasos(): Promise<void> {
    try {
      this.casos.set(await firstValueFrom(this.casoApi.listarOpcoes()));
    } catch (e) {
      console.error('[conciliacao] falha ao carregar os casos', e);
    }
  }

  tentarNovamente(): void {
    if (this.caso()) {
      void this.carregar();
      void this.carregarResumo();
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
    void this.carregarResumo();
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

  nomeTipo(tipo: TipoCategoria): string {
    return tipo === 'RECEITA' ? 'Receita' : 'Despesa';
  }

  nomeConta(contaBancariaId: string): string {
    return this.contas().find((conta) => conta.id === contaBancariaId)?.nome ?? '-';
  }

  nomeSituacao(situacao: SituacaoMovimentacao): string {
    return SITUACOES.find((opcao) => opcao.value === situacao)?.label ?? situacao;
  }

  nomeForma(forma: FormaConciliacao | null): string {
    return FORMAS.find((opcao) => opcao.value === forma)?.label ?? '-';
  }

  severidadeSituacao(situacao: SituacaoMovimentacao): 'success' | 'info' | 'secondary' {
    if (situacao === 'CONCILIADA') {
      return 'success';
    }
    return situacao === 'IGNORADA' ? 'info' : 'secondary';
  }

  conciliar(): void {
    this.confirmacao.confirm({
      header: 'Conciliar automaticamente',
      message: `Deseja conciliar automaticamente as movimentações pendentes ${
        this.filtro.contaBancariaId ? 'da conta selecionada' : 'de todas as contas do caso'
      }? Só são conciliadas as que têm um lançamento com a mesma conta, tipo, valor e data.`,
      icon: 'pi pi-question-circle',
      acceptLabel: 'Conciliar',
      rejectLabel: 'Cancelar',
      rejectButtonProps: { severity: 'secondary', outlined: true },
      accept: async () => {
        this.conciliando.set(true);
        try {
          const resultado = await firstValueFrom(
            this.api.conciliarAutomaticamente(this.casoId()!, this.filtro.contaBancariaId),
          );
          this.mensagens.add({
            severity: resultado.conciliadas > 0 ? 'success' : 'info',
            summary: 'Conciliação concluída.',
            detail: `Conciliadas: ${resultado.conciliadas} · Pendentes: ${resultado.pendentes} · Ambíguas: ${resultado.ambiguas}`,
          });
          await Promise.all([this.carregar(), this.carregarResumo()]);
        } catch (e) {
          this.mensagens.add({ severity: 'error', summary: mensagemDeErro(e) });
        } finally {
          this.conciliando.set(false);
        }
      },
    });
  }

  private aplicarPeriodoPadrao(caso: CasoResponse): void {
    const periodo = caso.situacao === 'ATIVO' ? caso.periodoAtual : null;
    this.periodo = periodo
      ? [parseLocalDate(periodo.dataInicio), parseLocalDate(periodo.dataFim)]
      : null;
    this.filtro.dataInicio = periodo?.dataInicio ?? null;
    this.filtro.dataFim = periodo?.dataFim ?? null;
  }

  private filtroVazio(): ConciliacaoFiltro {
    return {
      dataInicio: null,
      dataFim: null,
      contaBancariaId: null,
      situacao: 'PENDENTE',
    };
  }
}
