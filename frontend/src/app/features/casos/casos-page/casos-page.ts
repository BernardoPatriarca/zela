import { DatePipe } from '@angular/common';
import { Component, DestroyRef, inject, signal } from '@angular/core';
import { FormControl, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { ConfirmationService, MessageService } from 'primeng/api';
import { Button } from 'primeng/button';
import { ConfirmDialog } from 'primeng/confirmdialog';
import { DatePicker } from 'primeng/datepicker';
import { Dialog } from 'primeng/dialog';
import { IconField } from 'primeng/iconfield';
import { InputIcon } from 'primeng/inputicon';
import { InputMask } from 'primeng/inputmask';
import { InputNumber } from 'primeng/inputnumber';
import { InputText } from 'primeng/inputtext';
import { Message } from 'primeng/message';
import { Select } from 'primeng/select';
import { SelectButton } from 'primeng/selectbutton';
import { TableLazyLoadEvent, TableModule } from 'primeng/table';
import { Tag } from 'primeng/tag';
import { Toast } from 'primeng/toast';
import { Tooltip } from 'primeng/tooltip';
import { CasoApi, CasoFiltro, CasoRequest, CasoResponse } from '../caso-api';
import { criarCasoForm, SITUACOES_CASO, TIPOS_CASO } from '../caso.form';
import { ComarcaApi } from '../../comarcas/comarca-api';
import { PessoaApi } from '../../pessoas/pessoa-api';
import { VaraApi } from '../../varas/vara-api';
import { CnjPipe } from '../../../shared/pipes/cnj-pipe';
import { formatLocalDate, parseLocalDate } from '../../../shared/util/data';
import { mensagemDeErro } from '../../../shared/util/erro';
import { inicioDaUltimaPagina, ITENS_POR_PAGINA, Opcao } from '../../../shared/util/pagina';

@Component({
  imports: [
    FormsModule,
    ReactiveFormsModule,
    DatePipe,
    CnjPipe,
    Button,
    ConfirmDialog,
    DatePicker,
    Dialog,
    IconField,
    InputIcon,
    InputMask,
    InputNumber,
    InputText,
    Message,
    Select,
    SelectButton,
    TableModule,
    Tag,
    Toast,
    Tooltip,
  ],
  providers: [ConfirmationService, MessageService],
  selector: 'app-casos-page',
  templateUrl: './casos-page.html',
})
export class CasosPage {
  private readonly api = inject(CasoApi);
  private readonly comarcaApi = inject(ComarcaApi);
  private readonly varaApi = inject(VaraApi);
  private readonly pessoaApi = inject(PessoaApi);
  private readonly router = inject(Router);
  private readonly confirmacao = inject(ConfirmationService);
  private readonly mensagens = inject(MessageService);

  readonly casos = signal<CasoResponse[]>([]);
  readonly comarcas = signal<Opcao[]>([]);
  readonly varas = signal<Opcao[]>([]);
  readonly pessoas = signal<Opcao[]>([]);
  readonly total = signal(0);
  readonly primeiro = signal(0);
  readonly linhas = ITENS_POR_PAGINA;
  readonly carregando = signal(true);
  readonly erroLista = signal(false);

  readonly dialogAberto = signal(false);
  readonly editandoId = signal<string | null>(null);
  readonly salvando = signal(false);
  readonly erro = signal<string | null>(null);

  readonly casoEncerrando = signal<CasoResponse | null>(null);
  readonly encerrando = signal(false);
  readonly erroEncerramento = signal<string | null>(null);
  readonly dataEncerramento = new FormControl<Date | null>(null, Validators.required);

  readonly tipos = TIPOS_CASO;
  readonly situacoes = SITUACOES_CASO;
  readonly hoje = new Date();
  readonly form = criarCasoForm();

  readonly filtro: CasoFiltro = { tipo: null, comarcaId: null, situacao: 'ATIVO', busca: '' };
  private timerBusca?: ReturnType<typeof setTimeout>;
  private ultimaRequisicao = 0;

  constructor() {
    inject(DestroyRef).onDestroy(() => clearTimeout(this.timerBusca));
    void this.carregarOpcoes();
  }

  get numeroProcesso() {
    return this.form.controls.numeroProcesso;
  }

  get comarcaId() {
    return this.form.controls.comarcaId;
  }

  get varaId() {
    return this.form.controls.varaId;
  }

  get pessoaProtegidaId() {
    return this.form.controls.pessoaProtegidaId;
  }

  get responsavelId() {
    return this.form.controls.responsavelId;
  }

  get periodicidadeMeses() {
    return this.form.controls.periodicidadeMeses;
  }

  get prazoEntregaDias() {
    return this.form.controls.prazoEntregaDias;
  }

  get inicioPeriodo() {
    return this.form.controls.inicioPeriodo;
  }

  async carregar(): Promise<void> {
    const requisicao = ++this.ultimaRequisicao;
    this.carregando.set(true);
    this.erroLista.set(false);

    try {
      const pagina = await firstValueFrom(
        this.api.listar(this.filtro, this.primeiro() / this.linhas, this.linhas),
      );
      if (requisicao === this.ultimaRequisicao) {
        if (pagina.itens.length === 0 && this.primeiro() > 0) {
          this.primeiro.set(inicioDaUltimaPagina(pagina.total, this.linhas));
          void this.carregar();
          return;
        }
        this.casos.set(pagina.itens);
        this.total.set(pagina.total);
      }
    } catch (e) {
      if (requisicao === this.ultimaRequisicao) {
        console.error('[casos] falha ao carregar a lista', e);
        this.erroLista.set(true);
      }
    } finally {
      if (requisicao === this.ultimaRequisicao) {
        this.carregando.set(false);
      }
    }
  }

  async carregarOpcoes(): Promise<void> {
    try {
      const [comarcas, pessoas] = await Promise.all([
        firstValueFrom(this.comarcaApi.listarOpcoes()),
        firstValueFrom(this.pessoaApi.listarOpcoes()),
      ]);
      this.comarcas.set(comarcas);
      this.pessoas.set(pessoas);
    } catch (e) {
      console.error('[casos] falha ao carregar comarcas e pessoas', e);
    }
  }

  async carregarVaras(comarcaId: string | null): Promise<void> {
    this.varas.set([]);
    if (!comarcaId) {
      return;
    }

    try {
      this.varas.set(await firstValueFrom(this.varaApi.listarOpcoes(comarcaId)));
    } catch (e) {
      console.error('[casos] falha ao carregar as varas', e);
    }
  }

  paginar(evento: TableLazyLoadEvent): void {
    this.primeiro.set(evento.first ?? 0);
    void this.carregar();
  }

  buscar(valor: string): void {
    clearTimeout(this.timerBusca);
    this.timerBusca = setTimeout(() => {
      this.filtro.busca = valor;
      this.primeiro.set(0);
      void this.carregar();
    }, 300);
  }

  filtrar(): void {
    this.primeiro.set(0);
    void this.carregar();
  }

  trocarComarca(comarcaId: string | null): void {
    this.varaId.setValue(null);
    void this.carregarVaras(comarcaId);
  }

  novo(): void {
    this.editandoId.set(null);
    this.erro.set(null);
    this.varas.set([]);
    this.form.reset({
      tipo: 'CURATELA',
      numeroProcesso: '',
      comarcaId: null,
      varaId: null,
      pessoaProtegidaId: null,
      responsavelId: null,
      periodicidadeMeses: 12,
      prazoEntregaDias: 30,
      inicioPeriodo: null,
    });
    this.dialogAberto.set(true);
  }

  editar(caso: CasoResponse): void {
    this.editandoId.set(caso.id);
    this.erro.set(null);
    this.form.reset({
      tipo: caso.tipo,
      numeroProcesso: caso.numeroProcesso,
      comarcaId: caso.comarcaId,
      varaId: caso.varaId,
      pessoaProtegidaId: caso.pessoaProtegidaId,
      responsavelId: caso.responsavelId,
      periodicidadeMeses: caso.periodicidadeMeses,
      prazoEntregaDias: caso.prazoEntregaDias,
      inicioPeriodo: caso.periodoAtual ? parseLocalDate(caso.periodoAtual.dataInicio) : null,
    });
    void this.carregarVaras(caso.comarcaId);
    this.dialogAberto.set(true);
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
    const caso: CasoRequest = {
      tipo: valores.tipo,
      numeroProcesso: valores.numeroProcesso,
      varaId: valores.varaId!,
      pessoaProtegidaId: valores.pessoaProtegidaId!,
      responsavelId: valores.responsavelId!,
      periodicidadeMeses: valores.periodicidadeMeses!,
      prazoEntregaDias: valores.prazoEntregaDias!,
      inicioPeriodo: formatLocalDate(valores.inicioPeriodo!),
    };

    try {
      const id = this.editandoId();
      await firstValueFrom(id ? this.api.atualizar(id, caso) : this.api.criar(caso));
      this.dialogAberto.set(false);
      this.mensagens.add({
        severity: 'success',
        summary: id ? 'Caso atualizado.' : 'Caso cadastrado.',
      });
      await this.carregar();
    } catch (e) {
      this.erro.set(mensagemDeErro(e));
    } finally {
      this.salvando.set(false);
    }
  }

  abrirContas(caso: CasoResponse): void {
    void this.router.navigate(['/contas-bancarias'], { queryParams: { casoId: caso.id } });
  }

  abrirEncerramento(caso: CasoResponse): void {
    this.erroEncerramento.set(null);
    this.dataEncerramento.reset(new Date());
    this.casoEncerrando.set(caso);
  }

  fecharEncerramento(): void {
    this.casoEncerrando.set(null);
  }

  async encerrar(): Promise<void> {
    const caso = this.casoEncerrando();
    if (!caso || this.dataEncerramento.invalid) {
      this.dataEncerramento.markAsTouched();
      return;
    }

    this.encerrando.set(true);
    this.erroEncerramento.set(null);

    try {
      await firstValueFrom(
        this.api.encerrar(caso.id, formatLocalDate(this.dataEncerramento.value!)),
      );
      this.casoEncerrando.set(null);
      this.mensagens.add({ severity: 'success', summary: 'Caso encerrado.' });
      await this.carregar();
    } catch (e) {
      this.erroEncerramento.set(mensagemDeErro(e));
    } finally {
      this.encerrando.set(false);
    }
  }

  reabrir(caso: CasoResponse): void {
    this.confirmacao.confirm({
      header: 'Reabrir caso',
      message: `Deseja reabrir o caso de ${caso.pessoaProtegidaNome}?`,
      icon: 'pi pi-question-circle',
      acceptLabel: 'Reabrir',
      rejectLabel: 'Cancelar',
      rejectButtonProps: { severity: 'secondary', outlined: true },
      accept: async () => {
        try {
          await firstValueFrom(this.api.reabrir(caso.id));
          this.mensagens.add({ severity: 'success', summary: 'Caso reaberto.' });
          await this.carregar();
        } catch (e) {
          this.mensagens.add({ severity: 'error', summary: mensagemDeErro(e) });
        }
      },
    });
  }

  excluir(caso: CasoResponse): void {
    this.confirmacao.confirm({
      header: 'Excluir caso',
      message: `Deseja excluir o caso de ${caso.pessoaProtegidaNome}?`,
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: 'Excluir',
      rejectLabel: 'Cancelar',
      acceptButtonProps: { severity: 'danger' },
      rejectButtonProps: { severity: 'secondary', outlined: true },
      accept: async () => {
        try {
          await firstValueFrom(this.api.excluir(caso.id));
          this.mensagens.add({ severity: 'success', summary: 'Caso excluído.' });
          await this.carregar();
        } catch (e) {
          this.mensagens.add({ severity: 'error', summary: mensagemDeErro(e) });
        }
      },
    });
  }
}
