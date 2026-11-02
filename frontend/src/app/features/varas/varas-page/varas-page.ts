import { Component, DestroyRef, inject, signal } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import { ConfirmationService, MessageService } from 'primeng/api';
import { Button } from 'primeng/button';
import { ConfirmDialog } from 'primeng/confirmdialog';
import { Dialog } from 'primeng/dialog';
import { IconField } from 'primeng/iconfield';
import { InputIcon } from 'primeng/inputicon';
import { InputText } from 'primeng/inputtext';
import { Message } from 'primeng/message';
import { Select } from 'primeng/select';
import { TableLazyLoadEvent, TableModule } from 'primeng/table';
import { Tag } from 'primeng/tag';
import { Toast } from 'primeng/toast';
import { VaraApi, VaraRequest, VaraResponse } from '../vara-api';
import { criarVaraForm } from '../vara.form';
import { ComarcaApi } from '../../comarcas/comarca-api';
import { mensagemDeErro } from '../../../shared/util/erro';
import { inicioDaUltimaPagina, ITENS_POR_PAGINA, Opcao } from '../../../shared/util/pagina';

@Component({
  imports: [
    FormsModule,
    ReactiveFormsModule,
    Button,
    ConfirmDialog,
    Dialog,
    IconField,
    InputIcon,
    InputText,
    Message,
    Select,
    TableModule,
    Tag,
    Toast,
  ],
  providers: [ConfirmationService, MessageService],
  selector: 'app-varas-page',
  templateUrl: './varas-page.html',
})
export class VarasPage {
  private readonly api = inject(VaraApi);
  private readonly comarcaApi = inject(ComarcaApi);
  private readonly confirmacao = inject(ConfirmationService);
  private readonly mensagens = inject(MessageService);

  readonly varas = signal<VaraResponse[]>([]);
  readonly comarcas = signal<Opcao[]>([]);
  readonly total = signal(0);
  readonly primeiro = signal(0);
  readonly linhas = ITENS_POR_PAGINA;
  readonly carregando = signal(true);
  readonly erroLista = signal(false);

  readonly dialogAberto = signal(false);
  readonly editandoId = signal<string | null>(null);
  readonly salvando = signal(false);
  readonly erro = signal<string | null>(null);

  readonly form = criarVaraForm();

  comarcaFiltro: string | null = null;
  private busca = '';
  private timerBusca?: ReturnType<typeof setTimeout>;
  private ultimaRequisicao = 0;

  constructor() {
    inject(DestroyRef).onDestroy(() => clearTimeout(this.timerBusca));
    void this.carregarComarcas();
  }

  get comarcaId() {
    return this.form.controls.comarcaId;
  }

  get nome() {
    return this.form.controls.nome;
  }

  async carregar(): Promise<void> {
    const requisicao = ++this.ultimaRequisicao;
    this.carregando.set(true);
    this.erroLista.set(false);

    try {
      const pagina = await firstValueFrom(
        this.api.listar(this.comarcaFiltro, this.busca, this.primeiro() / this.linhas, this.linhas),
      );
      if (requisicao === this.ultimaRequisicao) {
        if (pagina.itens.length === 0 && this.primeiro() > 0) {
          this.primeiro.set(inicioDaUltimaPagina(pagina.total, this.linhas));
          void this.carregar();
          return;
        }
        this.varas.set(pagina.itens);
        this.total.set(pagina.total);
      }
    } catch (e) {
      if (requisicao === this.ultimaRequisicao) {
        console.error('[varas] falha ao carregar a lista', e);
        this.erroLista.set(true);
      }
    } finally {
      if (requisicao === this.ultimaRequisicao) {
        this.carregando.set(false);
      }
    }
  }

  async carregarComarcas(): Promise<void> {
    try {
      this.comarcas.set(await firstValueFrom(this.comarcaApi.listarOpcoes()));
    } catch (e) {
      console.error('[varas] falha ao carregar as comarcas', e);
    }
  }

  paginar(evento: TableLazyLoadEvent): void {
    this.primeiro.set(evento.first ?? 0);
    void this.carregar();
  }

  buscar(valor: string): void {
    clearTimeout(this.timerBusca);
    this.timerBusca = setTimeout(() => {
      this.busca = valor;
      this.primeiro.set(0);
      void this.carregar();
    }, 300);
  }

  filtrarPorComarca(comarcaId: string | null): void {
    this.comarcaFiltro = comarcaId;
    this.primeiro.set(0);
    void this.carregar();
  }

  nova(): void {
    this.editandoId.set(null);
    this.erro.set(null);
    this.form.reset({ comarcaId: this.comarcaFiltro, nome: '' });
    this.dialogAberto.set(true);
  }

  editar(vara: VaraResponse): void {
    this.editandoId.set(vara.id);
    this.erro.set(null);
    this.form.reset({ comarcaId: vara.comarcaId, nome: vara.nome });
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

    const { comarcaId, nome } = this.form.getRawValue();
    const vara: VaraRequest = { comarcaId: comarcaId!, nome: nome.trim() };

    try {
      const id = this.editandoId();
      await firstValueFrom(id ? this.api.atualizar(id, vara) : this.api.criar(vara));
      this.dialogAberto.set(false);
      this.mensagens.add({
        severity: 'success',
        summary: id ? 'Vara atualizada.' : 'Vara cadastrada.',
      });
      await this.carregar();
    } catch (e) {
      this.erro.set(mensagemDeErro(e));
    } finally {
      this.salvando.set(false);
    }
  }

  excluir(vara: VaraResponse): void {
    this.confirmacao.confirm({
      header: 'Excluir vara',
      message: `Deseja excluir a ${vara.nome} de ${vara.comarcaNome}?`,
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: 'Excluir',
      rejectLabel: 'Cancelar',
      acceptButtonProps: { severity: 'danger' },
      rejectButtonProps: { severity: 'secondary', outlined: true },
      accept: async () => {
        try {
          await firstValueFrom(this.api.excluir(vara.id));
          this.mensagens.add({ severity: 'success', summary: 'Vara excluída.' });
          await this.carregar();
        } catch (e) {
          this.mensagens.add({ severity: 'error', summary: mensagemDeErro(e) });
        }
      },
    });
  }
}
