import { DatePipe } from '@angular/common';
import { Component, effect, inject, input, signal, untracked } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { ConfirmationService, MessageService } from 'primeng/api';
import { Button } from 'primeng/button';
import { ConfirmDialog } from 'primeng/confirmdialog';
import { DatePicker } from 'primeng/datepicker';
import { Dialog } from 'primeng/dialog';
import { InputNumber } from 'primeng/inputnumber';
import { InputText } from 'primeng/inputtext';
import { Message } from 'primeng/message';
import { Select } from 'primeng/select';
import { TableLazyLoadEvent, TableModule } from 'primeng/table';
import { Toast } from 'primeng/toast';
import {
  Banco,
  ContaBancariaApi,
  ContaBancariaRequest,
  ContaBancariaResponse,
  TipoConta,
} from '../conta-bancaria-api';
import { BANCOS, criarContaBancariaForm, TIPOS_CONTA } from '../conta-bancaria.form';
import { CasoApi, CasoResponse } from '../../casos/caso-api';
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
    ConfirmDialog,
    DatePicker,
    Dialog,
    InputNumber,
    InputText,
    Message,
    Select,
    TableModule,
    Toast,
  ],
  providers: [ConfirmationService, MessageService],
  selector: 'app-contas-bancarias-page',
  templateUrl: './contas-bancarias-page.html',
})
export class ContasBancariasPage {
  private readonly api = inject(ContaBancariaApi);
  private readonly casoApi = inject(CasoApi);
  private readonly router = inject(Router);
  private readonly rota = inject(ActivatedRoute);
  private readonly confirmacao = inject(ConfirmationService);
  private readonly mensagens = inject(MessageService);

  readonly casoId = input<string>();

  readonly casos = signal<Opcao[]>([]);
  readonly caso = signal<CasoResponse | null>(null);
  readonly contas = signal<ContaBancariaResponse[]>([]);
  readonly total = signal(0);
  readonly primeiro = signal(0);
  readonly linhas = ITENS_POR_PAGINA;
  readonly carregando = signal(false);
  readonly erroLista = signal(false);

  readonly dialogAberto = signal(false);
  readonly editandoId = signal<string | null>(null);
  readonly salvando = signal(false);
  readonly erro = signal<string | null>(null);

  readonly bancos = BANCOS;
  readonly tiposConta = TIPOS_CONTA;
  readonly hoje = new Date();
  readonly form = criarContaBancariaForm();

  private ultimaRequisicao = 0;

  constructor() {
    void this.carregarCasos();
    effect(() => {
      const casoId = this.casoId();
      untracked(() => {
        this.primeiro.set(0);
        this.caso.set(null);
        this.contas.set([]);
        this.total.set(0);
        if (casoId) {
          void this.carregar();
        }
      });
    });
  }

  get banco() {
    return this.form.controls.banco;
  }

  get agencia() {
    return this.form.controls.agencia;
  }

  get numero() {
    return this.form.controls.numero;
  }

  get saldoInicial() {
    return this.form.controls.saldoInicial;
  }

  get dataSaldoInicial() {
    return this.form.controls.dataSaldoInicial;
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
      const [caso, pagina] = await Promise.all([
        firstValueFrom(this.casoApi.buscarPorId(casoId)),
        firstValueFrom(this.api.listar(casoId, this.primeiro() / this.linhas, this.linhas)),
      ]);
      if (requisicao === this.ultimaRequisicao) {
        if (pagina.itens.length === 0 && this.primeiro() > 0) {
          this.primeiro.set(inicioDaUltimaPagina(pagina.total, this.linhas));
          void this.carregar();
          return;
        }
        this.caso.set(caso);
        this.contas.set(pagina.itens);
        this.total.set(pagina.total);
      }
    } catch (e) {
      if (requisicao === this.ultimaRequisicao) {
        console.error('[contas-bancarias] falha ao carregar as contas', e);
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
      console.error('[contas-bancarias] falha ao carregar os casos', e);
    }
  }

  selecionarCaso(casoId: string | null): void {
    void this.router.navigate([], { relativeTo: this.rota, queryParams: { casoId } });
  }

  paginar(evento: TableLazyLoadEvent): void {
    this.primeiro.set(evento.first ?? 0);
    void this.carregar();
  }

  nomeBanco(banco: Banco): string {
    return this.bancos.find((opcao) => opcao.value === banco)?.label ?? banco;
  }

  nomeTipo(tipo: TipoConta): string {
    return this.tiposConta.find((opcao) => opcao.value === tipo)?.label ?? tipo;
  }

  nova(): void {
    this.editandoId.set(null);
    this.erro.set(null);
    this.form.reset({
      banco: null,
      tipo: 'CORRENTE',
      agencia: '',
      numero: '',
      descricao: '',
      saldoInicial: null,
      dataSaldoInicial: null,
    });
    this.dialogAberto.set(true);
  }

  editar(conta: ContaBancariaResponse): void {
    this.editandoId.set(conta.id);
    this.erro.set(null);
    this.form.reset({
      banco: conta.banco,
      tipo: conta.tipo,
      agencia: conta.agencia,
      numero: conta.numero,
      descricao: conta.descricao ?? '',
      saldoInicial: conta.saldoInicial,
      dataSaldoInicial: parseLocalDate(conta.dataSaldoInicial),
    });
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
    const conta: ContaBancariaRequest = {
      banco: valores.banco!,
      tipo: valores.tipo,
      agencia: valores.agencia.trim(),
      numero: valores.numero.trim(),
      descricao: valores.descricao.trim() || null,
      saldoInicial: valores.saldoInicial!,
      dataSaldoInicial: formatLocalDate(valores.dataSaldoInicial!),
    };

    try {
      const id = this.editandoId();
      await firstValueFrom(
        id ? this.api.atualizar(this.casoId()!, id, conta) : this.api.criar(this.casoId()!, conta),
      );
      this.dialogAberto.set(false);
      this.mensagens.add({
        severity: 'success',
        summary: id ? 'Conta bancária atualizada.' : 'Conta bancária cadastrada.',
      });
      await this.carregar();
    } catch (e) {
      this.erro.set(mensagemDeErro(e));
    } finally {
      this.salvando.set(false);
    }
  }

  excluir(conta: ContaBancariaResponse): void {
    this.confirmacao.confirm({
      header: 'Excluir conta bancária',
      message: `Deseja excluir a conta ${conta.agencia} / ${conta.numero}?`,
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: 'Excluir',
      rejectLabel: 'Cancelar',
      acceptButtonProps: { severity: 'danger' },
      rejectButtonProps: { severity: 'secondary', outlined: true },
      accept: async () => {
        try {
          await firstValueFrom(this.api.excluir(this.casoId()!, conta.id));
          this.mensagens.add({ severity: 'success', summary: 'Conta bancária excluída.' });
          await this.carregar();
        } catch (e) {
          this.mensagens.add({ severity: 'error', summary: mensagemDeErro(e) });
        }
      },
    });
  }
}
