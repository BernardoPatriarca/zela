import { DatePipe } from '@angular/common';
import { Component, effect, inject, input, signal, untracked } from '@angular/core';
import { FormControl, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { MessageService } from 'primeng/api';
import { Button } from 'primeng/button';
import { DatePicker } from 'primeng/datepicker';
import { Dialog } from 'primeng/dialog';
import { InputNumber } from 'primeng/inputnumber';
import { InputText } from 'primeng/inputtext';
import { Message } from 'primeng/message';
import { Select } from 'primeng/select';
import { TableLazyLoadEvent, TableModule } from 'primeng/table';
import { Tag } from 'primeng/tag';
import { Textarea } from 'primeng/textarea';
import { Toast } from 'primeng/toast';
import { LancamentoApi, LancamentoFiltro, LancamentoResponse } from '../lancamento-api';
import { criarLancamentoForm, SITUACOES_LANCAMENTO, TIPOS_LANCAMENTO } from '../lancamento.form';
import { CasoApi, CasoResponse } from '../../casos/caso-api';
import { CategoriaApi, TipoCategoria } from '../../categorias/categoria-api';
import { ContaBancariaApi } from '../../contas-bancarias/conta-bancaria-api';
import { MoedaPipe } from '../../../shared/pipes/moeda-pipe';
import { formatLocalDate, parseLocalDate } from '../../../shared/util/data';
import { mensagemDeErro } from '../../../shared/util/erro';
import { inicioDaUltimaPagina, ITENS_POR_PAGINA, Opcao } from '../../../shared/util/pagina';

@Component({
  imports: [
    FormsModule,
    ReactiveFormsModule,
    DatePipe,
    MoedaPipe,
    Button,
    DatePicker,
    Dialog,
    InputNumber,
    InputText,
    Message,
    Select,
    TableModule,
    Tag,
    Textarea,
    Toast,
  ],
  providers: [MessageService],
  selector: 'app-lancamentos-page',
  templateUrl: './lancamentos-page.html',
})
export class LancamentosPage {
  private readonly api = inject(LancamentoApi);
  private readonly casoApi = inject(CasoApi);
  private readonly contaApi = inject(ContaBancariaApi);
  private readonly categoriaApi = inject(CategoriaApi);
  private readonly router = inject(Router);
  private readonly rota = inject(ActivatedRoute);
  private readonly mensagens = inject(MessageService);

  readonly casoId = input<string>();

  readonly casos = signal<Opcao[]>([]);
  readonly caso = signal<CasoResponse | null>(null);
  readonly contas = signal<Opcao[]>([]);
  readonly categoriasFiltro = signal<Opcao[]>([]);
  readonly categoriasForm = signal<Opcao[]>([]);
  readonly lancamentos = signal<LancamentoResponse[]>([]);
  readonly total = signal(0);
  readonly primeiro = signal(0);
  readonly linhas = ITENS_POR_PAGINA;
  readonly carregando = signal(false);
  readonly erroLista = signal(false);

  readonly dialogAberto = signal(false);
  readonly editandoId = signal<string | null>(null);
  readonly salvando = signal(false);
  readonly erro = signal<string | null>(null);

  readonly lancamentoExcluindo = signal<LancamentoResponse | null>(null);
  readonly excluindo = signal(false);
  readonly erroExclusao = signal<string | null>(null);
  readonly justificativaExclusao = new FormControl('', {
    nonNullable: true,
    validators: [Validators.required, Validators.minLength(10), Validators.maxLength(500)],
  });

  readonly tipos = TIPOS_LANCAMENTO;
  readonly situacoes = SITUACOES_LANCAMENTO;
  readonly hoje = new Date();
  readonly form = criarLancamentoForm();

  filtro: LancamentoFiltro = this.filtroVazio();
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
        this.categoriasFiltro.set([]);
        this.lancamentos.set([]);
        this.total.set(0);
        this.filtro = this.filtroVazio();
        this.periodo = null;
        if (casoId) {
          void this.carregarCaso();
        }
      });
    });
  }

  get contaBancariaId() {
    return this.form.controls.contaBancariaId;
  }

  get categoriaId() {
    return this.form.controls.categoriaId;
  }

  get dataLancamento() {
    return this.form.controls.dataLancamento;
  }

  get valor() {
    return this.form.controls.valor;
  }

  get descricao() {
    return this.form.controls.descricao;
  }

  get justificativa() {
    return this.form.controls.justificativa;
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
        console.error('[lancamentos] falha ao carregar o caso', e);
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
        this.lancamentos.set(pagina.itens);
        this.total.set(pagina.total);
      }
    } catch (e) {
      if (requisicao === this.ultimaRequisicao) {
        console.error('[lancamentos] falha ao carregar os lançamentos', e);
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
      console.error('[lancamentos] falha ao carregar os casos', e);
    }
  }

  async carregarCategoriasFiltro(tipo: TipoCategoria): Promise<void> {
    const opcoes = await this.buscarCategorias(tipo);
    if (this.filtro.tipo === tipo) {
      this.categoriasFiltro.set(opcoes);
    }
  }

  async carregarCategoriasForm(tipo: TipoCategoria): Promise<void> {
    const opcoes = await this.buscarCategorias(tipo);
    if (this.form.controls.tipo.value === tipo) {
      this.categoriasForm.set(opcoes);
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

  filtrarPorTipo(): void {
    this.filtro.categoriaId = null;
    this.categoriasFiltro.set([]);
    if (this.filtro.tipo) {
      void this.carregarCategoriasFiltro(this.filtro.tipo);
    }
    this.filtrar();
  }

  nomeTipo(tipo: TipoCategoria): string {
    return this.tipos.find((opcao) => opcao.value === tipo)?.label ?? tipo;
  }

  nomeConta(contaBancariaId: string): string {
    return this.contas().find((conta) => conta.id === contaBancariaId)?.nome ?? '-';
  }

  nova(): void {
    this.editandoId.set(null);
    this.erro.set(null);
    this.categoriasForm.set([]);
    this.form.reset({
      tipo: 'DESPESA',
      dataLancamento: new Date(),
      valor: null,
      contaBancariaId: null,
      categoriaId: null,
      descricao: '',
      justificativa: '',
    });
    this.form.controls.tipo.enable();
    this.form.controls.justificativa.disable();
    void this.carregarCategoriasForm('DESPESA');
    this.dialogAberto.set(true);
  }

  editar(lancamento: LancamentoResponse): void {
    this.editandoId.set(lancamento.id);
    this.erro.set(null);
    this.categoriasForm.set([]);
    this.form.reset({
      tipo: lancamento.tipo,
      dataLancamento: parseLocalDate(lancamento.dataLancamento),
      valor: lancamento.valor,
      contaBancariaId: lancamento.contaBancariaId,
      categoriaId: lancamento.categoriaId,
      descricao: lancamento.descricao,
      justificativa: '',
    });
    this.form.controls.tipo.disable();
    this.form.controls.justificativa.enable();
    void this.carregarCategoriasForm(lancamento.tipo);
    this.dialogAberto.set(true);
  }

  trocarTipo(tipo: TipoCategoria): void {
    this.categoriaId.reset(null);
    this.categoriasForm.set([]);
    void this.carregarCategoriasForm(tipo);
  }

  async salvar(): Promise<void> {
    if (this.form.invalid) {
      this.form.markAllAsDirty();
      this.form.markAllAsTouched();
      return;
    }

    this.salvando.set(true);
    this.erro.set(null);

    const valores = this.form.getRawValue();
    const lancamento = {
      contaBancariaId: valores.contaBancariaId!,
      categoriaId: valores.categoriaId!,
      dataLancamento: formatLocalDate(valores.dataLancamento!),
      valor: valores.valor!,
      descricao: valores.descricao.trim(),
    };

    try {
      const id = this.editandoId();
      await firstValueFrom(
        id
          ? this.api.atualizar(this.casoId()!, id, {
              ...lancamento,
              justificativa: valores.justificativa.trim(),
            })
          : this.api.criar(this.casoId()!, { ...lancamento, tipo: valores.tipo }),
      );
      this.dialogAberto.set(false);
      this.mensagens.add({
        severity: 'success',
        summary: id ? 'Lançamento atualizado.' : 'Lançamento cadastrado.',
      });
      await this.carregar();
    } catch (e) {
      this.erro.set(mensagemDeErro(e));
    } finally {
      this.salvando.set(false);
    }
  }

  abrirExclusao(lancamento: LancamentoResponse): void {
    this.erroExclusao.set(null);
    this.justificativaExclusao.reset('');
    this.lancamentoExcluindo.set(lancamento);
  }

  fecharExclusao(): void {
    this.lancamentoExcluindo.set(null);
  }

  async excluir(): Promise<void> {
    const lancamento = this.lancamentoExcluindo();
    if (!lancamento || this.justificativaExclusao.invalid) {
      this.justificativaExclusao.markAsTouched();
      return;
    }

    this.excluindo.set(true);
    this.erroExclusao.set(null);

    try {
      await firstValueFrom(
        this.api.excluir(this.casoId()!, lancamento.id, this.justificativaExclusao.value.trim()),
      );
      this.lancamentoExcluindo.set(null);
      this.mensagens.add({ severity: 'success', summary: 'Lançamento excluído.' });
      await this.carregar();
    } catch (e) {
      this.erroExclusao.set(mensagemDeErro(e));
    } finally {
      this.excluindo.set(false);
    }
  }

  private async buscarCategorias(tipo: TipoCategoria): Promise<Opcao[]> {
    try {
      return await firstValueFrom(this.categoriaApi.listarOpcoes(tipo));
    } catch (e) {
      console.error('[lancamentos] falha ao carregar as categorias', e);
      return [];
    }
  }

  private aplicarPeriodoPadrao(caso: CasoResponse): void {
    const periodo = caso.situacao === 'ATIVO' ? caso.periodoAtual : null;
    this.periodo = periodo
      ? [parseLocalDate(periodo.dataInicio), parseLocalDate(periodo.dataFim)]
      : null;
    this.filtro.dataInicio = periodo?.dataInicio ?? null;
    this.filtro.dataFim = periodo?.dataFim ?? null;
  }

  private filtroVazio(): LancamentoFiltro {
    return {
      dataInicio: null,
      dataFim: null,
      contaBancariaId: null,
      tipo: null,
      categoriaId: null,
      situacao: null,
    };
  }
}
