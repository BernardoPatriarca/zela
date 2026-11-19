import { DatePipe } from '@angular/common';
import { Component, effect, inject, input, signal, untracked } from '@angular/core';
import { FormControl, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
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
import { Tag } from 'primeng/tag';
import { Textarea } from 'primeng/textarea';
import { Toast } from 'primeng/toast';
import {
  PendenciaResponse,
  PrestacaoContasApi,
  PrestacaoContasDetalhe,
  PrestacaoContasFiltro,
  PrestacaoContasResumo,
  RelatorioResponse,
  SituacaoPeriodo,
} from '../prestacao-contas-api';
import {
  criarDadosProfissionaisForm,
  NOMES_PENDENCIA,
  SITUACOES_PERIODO,
} from '../prestacao-contas.form';
import { UsuarioApi } from '../../../core/auth/usuario-api';
import { CasoApi } from '../../casos/caso-api';
import { CnjPipe } from '../../../shared/pipes/cnj-pipe';
import { MoedaPipe } from '../../../shared/pipes/moeda-pipe';
import { formatLocalDate, parseLocalDate } from '../../../shared/util/data';
import { mensagemDeErro } from '../../../shared/util/erro';
import { inicioDaUltimaPagina, ITENS_POR_PAGINA, Opcao } from '../../../shared/util/pagina';

interface SaldoEdicao {
  contaBancariaId: string;
  contaDescricao: string;
  saldoInicial: number | null;
  informado: boolean;
  saldoFinal: number | null;
}

@Component({
  imports: [
    FormsModule,
    ReactiveFormsModule,
    DatePipe,
    CnjPipe,
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
    Tag,
    Textarea,
    Toast,
  ],
  providers: [ConfirmationService, MessageService],
  selector: 'app-prestacao-contas-page',
  templateUrl: './prestacao-contas-page.html',
})
export class PrestacaoContasPage {
  private readonly api = inject(PrestacaoContasApi);
  private readonly casoApi = inject(CasoApi);
  private readonly usuarioApi = inject(UsuarioApi);
  private readonly router = inject(Router);
  private readonly rota = inject(ActivatedRoute);
  private readonly confirmacao = inject(ConfirmationService);
  private readonly mensagens = inject(MessageService);

  readonly casoId = input<string>();

  readonly casos = signal<Opcao[]>([]);
  readonly prestacoes = signal<PrestacaoContasResumo[]>([]);
  readonly total = signal(0);
  readonly primeiro = signal(0);
  readonly linhas = ITENS_POR_PAGINA;
  readonly carregando = signal(false);
  readonly erroLista = signal(false);

  readonly dialogAberto = signal(false);
  readonly abrindo = signal(false);
  readonly erroDialogo = signal<string | null>(null);
  readonly detalhe = signal<PrestacaoContasDetalhe | null>(null);
  readonly saldosEdicao = signal<SaldoEdicao[]>([]);
  readonly processando = signal(false);
  readonly salvandoSaldos = signal(false);
  readonly salvandoDados = signal(false);
  readonly notas = new FormControl('', {
    nonNullable: true,
    validators: [Validators.maxLength(4000)],
  });
  prazoCurador: Date | null = null;
  justificativas: Record<string, string> = {};

  readonly entregaAberta = signal(false);
  readonly entregando = signal(false);
  readonly erroEntrega = signal<string | null>(null);
  readonly dataEntrega = new FormControl<Date | null>(null, Validators.required);
  minimoEntrega: Date = new Date();

  readonly dadosAberto = signal(false);
  readonly salvandoPerfil = signal(false);
  readonly erroPerfil = signal<string | null>(null);
  readonly formPerfil = criarDadosProfissionaisForm();

  readonly situacoes = SITUACOES_PERIODO;
  readonly nomesPendencia = NOMES_PENDENCIA;
  readonly hoje = new Date();

  filtro: PrestacaoContasFiltro = { casoId: null, situacao: null };
  private ultimaRequisicao = 0;

  constructor() {
    void this.carregarCasos();
    effect(() => {
      const casoId = this.casoId();
      untracked(() => {
        this.filtro.casoId = casoId ?? null;
        this.primeiro.set(0);
        void this.carregar();
      });
    });
  }

  get nomeCompleto() {
    return this.formPerfil.controls.nomeCompleto;
  }

  get oabNumero() {
    return this.formPerfil.controls.oabNumero;
  }

  get oabUf() {
    return this.formPerfil.controls.oabUf;
  }

  get somenteLeitura(): boolean {
    const detalhe = this.detalhe();
    return !detalhe || detalhe.situacao === 'ENTREGUE' || detalhe.casoEncerrado;
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
        this.prestacoes.set(pagina.itens);
        this.total.set(pagina.total);
      }
    } catch (e) {
      if (requisicao === this.ultimaRequisicao) {
        console.error('[prestacao-contas] falha ao carregar as prestações', e);
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
      console.error('[prestacao-contas] falha ao carregar os casos', e);
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

  nomeSituacao(situacao: SituacaoPeriodo): string {
    return SITUACOES_PERIODO.find((opcao) => opcao.value === situacao)?.label ?? situacao;
  }

  severidadeSituacao(situacao: SituacaoPeriodo): 'secondary' | 'warn' | 'info' | 'success' {
    switch (situacao) {
      case 'EM_REVISAO':
        return 'warn';
      case 'PRONTO':
        return 'info';
      case 'ENTREGUE':
        return 'success';
      default:
        return 'secondary';
    }
  }

  chave(pendencia: PendenciaResponse): string {
    return `${pendencia.tipo}|${pendencia.referencia}`;
  }

  maximizar(janela: Dialog): void {
    if (!janela.maximized) {
      janela.maximize();
    }
  }

  async abrir(prestacao: PrestacaoContasResumo): Promise<void> {
    this.detalhe.set(null);
    this.erroDialogo.set(null);
    this.dialogAberto.set(true);
    await this.carregarDetalhe(prestacao.id);
  }

  async carregarDetalhe(id: string): Promise<void> {
    this.abrindo.set(true);
    this.erroDialogo.set(null);

    try {
      this.aplicarDetalhe(await firstValueFrom(this.api.buscarPorId(id)));
    } catch (e) {
      console.error('[prestacao-contas] falha ao carregar a prestação', e);
      this.erroDialogo.set(mensagemDeErro(e));
    } finally {
      this.abrindo.set(false);
    }
  }

  irPara(rota: string): void {
    const detalhe = this.detalhe();
    if (detalhe) {
      void this.router.navigate([rota], { queryParams: { casoId: detalhe.casoId } });
    }
  }

  async salvarSaldos(): Promise<void> {
    const detalhe = this.detalhe();
    if (!detalhe) {
      return;
    }

    const saldos = this.saldosEdicao()
      .filter((saldo) => saldo.saldoInicial !== null)
      .map((saldo) => ({
        contaBancariaId: saldo.contaBancariaId,
        saldoInicial: saldo.saldoInicial!,
      }));

    this.salvandoSaldos.set(true);
    try {
      await firstValueFrom(this.api.informarSaldosIniciais(detalhe.id, saldos));
      this.mensagens.add({ severity: 'success', summary: 'Saldos iniciais salvos.' });
      await this.carregarDetalhe(detalhe.id);
    } catch (e) {
      this.mensagens.add({ severity: 'error', summary: mensagemDeErro(e) });
    } finally {
      this.salvandoSaldos.set(false);
    }
  }

  async salvarDados(): Promise<void> {
    const detalhe = this.detalhe();
    if (!detalhe || this.notas.invalid) {
      this.notas.markAsTouched();
      return;
    }

    this.salvandoDados.set(true);
    try {
      this.aplicarDetalhe(
        await firstValueFrom(
          this.api.atualizar(
            detalhe.id,
            this.prazoCurador ? formatLocalDate(this.prazoCurador) : null,
            this.notas.value.trim(),
          ),
        ),
      );
      this.mensagens.add({ severity: 'success', summary: 'Dados da prestação salvos.' });
      void this.carregar();
    } catch (e) {
      this.mensagens.add({ severity: 'error', summary: mensagemDeErro(e) });
    } finally {
      this.salvandoDados.set(false);
    }
  }

  async salvarJustificativa(pendencia: PendenciaResponse): Promise<void> {
    const detalhe = this.detalhe();
    if (!detalhe || this.somenteLeitura) {
      return;
    }

    const texto = (this.justificativas[this.chave(pendencia)] ?? '').trim();
    if (texto === (pendencia.justificativa ?? '')) {
      return;
    }

    try {
      await firstValueFrom(
        this.api.salvarJustificativa(detalhe.id, {
          tipo: pendencia.tipo,
          referencia: pendencia.referencia,
          texto,
        }),
      );
      this.detalhe.update((atual) =>
        atual
          ? {
              ...atual,
              pendencias: atual.pendencias.map((item) =>
                this.chave(item) === this.chave(pendencia)
                  ? { ...item, justificativa: texto || null }
                  : item,
              ),
            }
          : atual,
      );
    } catch (e) {
      this.mensagens.add({ severity: 'error', summary: mensagemDeErro(e) });
    }
  }

  async receberDoCurador(): Promise<void> {
    const detalhe = this.detalhe();
    if (!detalhe) {
      return;
    }
    await this.executar(async () => {
      this.aplicarDetalhe(await firstValueFrom(this.api.receberDoCurador(detalhe.id)));
      this.mensagens.add({ severity: 'success', summary: 'Prestação de contas em revisão.' });
    });
  }

  async voltarParaRevisao(): Promise<void> {
    const detalhe = this.detalhe();
    if (!detalhe) {
      return;
    }
    await this.executar(async () => {
      this.aplicarDetalhe(await firstValueFrom(this.api.voltarParaRevisao(detalhe.id)));
      this.mensagens.add({ severity: 'success', summary: 'Prestação de contas em revisão.' });
    });
  }

  async gerarPdf(): Promise<void> {
    const detalhe = this.detalhe();
    if (!detalhe) {
      return;
    }

    try {
      const perfil = await firstValueFrom(this.usuarioApi.obterPerfil());
      if (!perfil.nomeCompleto || !perfil.oabNumero || !perfil.oabUf) {
        this.formPerfil.reset({
          nomeCompleto: perfil.nomeCompleto ?? '',
          oabNumero: perfil.oabNumero ?? '',
          oabUf: perfil.oabUf ?? '',
        });
        this.erroPerfil.set(null);
        this.dadosAberto.set(true);
        return;
      }
    } catch (e) {
      this.mensagens.add({ severity: 'error', summary: mensagemDeErro(e) });
      return;
    }

    this.confirmarGeracao();
  }

  async salvarPerfil(): Promise<void> {
    if (this.formPerfil.invalid) {
      this.formPerfil.markAllAsDirty();
      this.formPerfil.markAllAsTouched();
      return;
    }

    this.salvandoPerfil.set(true);
    this.erroPerfil.set(null);

    const valores = this.formPerfil.getRawValue();
    try {
      await firstValueFrom(
        this.usuarioApi.salvarDadosProfissionais({
          nomeCompleto: valores.nomeCompleto.trim(),
          oabNumero: valores.oabNumero.trim(),
          oabUf: valores.oabUf.trim().toUpperCase(),
        }),
      );
      this.dadosAberto.set(false);
      this.confirmarGeracao();
    } catch (e) {
      this.erroPerfil.set(mensagemDeErro(e));
    } finally {
      this.salvandoPerfil.set(false);
    }
  }

  async baixar(relatorio: RelatorioResponse): Promise<void> {
    const detalhe = this.detalhe();
    if (!detalhe) {
      return;
    }

    try {
      const arquivo = await firstValueFrom(this.api.baixarRelatorio(detalhe.id, relatorio.id));
      const url = URL.createObjectURL(arquivo);
      const link = document.createElement('a');
      link.href = url;
      link.download = `prestacao-contas-v${relatorio.versao}.pdf`;
      link.click();
      URL.revokeObjectURL(url);
    } catch (e) {
      this.mensagens.add({ severity: 'error', summary: mensagemDeErro(e) });
    }
  }

  abrirEntrega(): void {
    const detalhe = this.detalhe();
    if (!detalhe) {
      return;
    }

    this.minimoEntrega = parseLocalDate(detalhe.dataFim);
    this.erroEntrega.set(null);
    this.dataEntrega.reset(new Date());
    this.entregaAberta.set(true);
  }

  async entregar(): Promise<void> {
    const detalhe = this.detalhe();
    if (!detalhe || this.dataEntrega.invalid) {
      this.dataEntrega.markAsTouched();
      return;
    }

    this.entregando.set(true);
    this.erroEntrega.set(null);

    try {
      this.aplicarDetalhe(
        await firstValueFrom(
          this.api.entregar(detalhe.id, formatLocalDate(this.dataEntrega.value!)),
        ),
      );
      this.entregaAberta.set(false);
      this.mensagens.add({
        severity: 'success',
        summary: 'Prestação de contas entregue.',
        detail: 'O próximo período foi aberto.',
      });
      await this.carregar();
    } catch (e) {
      this.erroEntrega.set(mensagemDeErro(e));
    } finally {
      this.entregando.set(false);
    }
  }

  private confirmarGeracao(): void {
    const detalhe = this.detalhe();
    if (!detalhe) {
      return;
    }

    const ressalvas = detalhe.pendencias.length;
    this.confirmacao.confirm({
      header: 'Gerar PDF',
      message:
        ressalvas > 0
          ? `Gerar com ${ressalvas} ${ressalvas === 1 ? 'ressalva' : 'ressalvas'}?`
          : 'Deseja gerar o relatório em PDF?',
      icon: 'pi pi-question-circle',
      acceptLabel: 'Gerar',
      rejectLabel: 'Cancelar',
      rejectButtonProps: { severity: 'secondary', outlined: true },
      accept: () =>
        this.executar(async () => {
          for (const pendencia of detalhe.pendencias) {
            await this.salvarJustificativa(pendencia);
          }
          const relatorio = await firstValueFrom(this.api.gerarRelatorio(detalhe.id));
          this.mensagens.add({
            severity: 'success',
            summary: `Relatório v${relatorio.versao} gerado.`,
          });
          await this.carregarDetalhe(detalhe.id);
        }),
    });
  }

  private async executar(acao: () => Promise<void>): Promise<void> {
    if (this.processando()) {
      return;
    }
    this.processando.set(true);
    try {
      await acao();
      await this.carregar();
    } catch (e) {
      this.mensagens.add({ severity: 'error', summary: mensagemDeErro(e) });
    } finally {
      this.processando.set(false);
    }
  }

  private aplicarDetalhe(detalhe: PrestacaoContasDetalhe): void {
    this.detalhe.set(detalhe);
    this.saldosEdicao.set(
      detalhe.saldos.map((saldo) => ({
        contaBancariaId: saldo.contaBancariaId,
        contaDescricao: saldo.contaDescricao,
        saldoInicial: saldo.saldoInicial ?? saldo.saldoSugerido,
        informado: saldo.saldoInicial !== null,
        saldoFinal: saldo.saldoFinal,
      })),
    );
    this.prazoCurador = detalhe.prazoCurador ? parseLocalDate(detalhe.prazoCurador) : null;
    this.notas.reset(detalhe.notasExplicativas ?? '');
    if (this.somenteLeitura) {
      this.notas.disable();
    } else {
      this.notas.enable();
    }
    this.justificativas = {};
    for (const pendencia of detalhe.pendencias) {
      this.justificativas[this.chave(pendencia)] = pendencia.justificativa ?? '';
    }
  }
}
