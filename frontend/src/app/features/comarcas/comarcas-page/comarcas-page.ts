import { Component, DestroyRef, inject, signal } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
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
import { ComarcaApi, ComarcaRequest, ComarcaResponse } from '../comarca-api';
import { criarComarcaForm, UFS } from '../comarca.form';
import { mensagemDeErro } from '../../../shared/util/erro';
import { inicioDaUltimaPagina, ITENS_POR_PAGINA } from '../../../shared/util/pagina';

@Component({
  imports: [
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
  selector: 'app-comarcas-page',
  templateUrl: './comarcas-page.html',
})
export class ComarcasPage {
  private readonly api = inject(ComarcaApi);
  private readonly confirmacao = inject(ConfirmationService);
  private readonly mensagens = inject(MessageService);

  readonly comarcas = signal<ComarcaResponse[]>([]);
  readonly total = signal(0);
  readonly primeiro = signal(0);
  readonly linhas = ITENS_POR_PAGINA;
  readonly carregando = signal(true);
  readonly erroLista = signal(false);

  readonly dialogAberto = signal(false);
  readonly editandoId = signal<string | null>(null);
  readonly salvando = signal(false);
  readonly erro = signal<string | null>(null);

  readonly ufs = UFS;
  readonly form = criarComarcaForm();

  private busca = '';
  private timerBusca?: ReturnType<typeof setTimeout>;
  private ultimaRequisicao = 0;

  constructor() {
    inject(DestroyRef).onDestroy(() => clearTimeout(this.timerBusca));
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
        this.api.listar(this.busca, this.primeiro() / this.linhas, this.linhas),
      );
      if (requisicao === this.ultimaRequisicao) {
        if (pagina.itens.length === 0 && this.primeiro() > 0) {
          this.primeiro.set(inicioDaUltimaPagina(pagina.total, this.linhas));
          void this.carregar();
          return;
        }
        this.comarcas.set(pagina.itens);
        this.total.set(pagina.total);
      }
    } catch (e) {
      if (requisicao === this.ultimaRequisicao) {
        console.error('[comarcas] falha ao carregar a lista', e);
        this.erroLista.set(true);
      }
    } finally {
      if (requisicao === this.ultimaRequisicao) {
        this.carregando.set(false);
      }
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

  nova(): void {
    this.editandoId.set(null);
    this.erro.set(null);
    this.form.reset();
    this.dialogAberto.set(true);
  }

  editar(comarca: ComarcaResponse): void {
    this.editandoId.set(comarca.id);
    this.erro.set(null);
    this.form.reset({ nome: comarca.nome, uf: comarca.uf });
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

    const { nome, uf } = this.form.getRawValue();
    const comarca: ComarcaRequest = { nome: nome.trim(), uf };

    try {
      const id = this.editandoId();
      await firstValueFrom(id ? this.api.atualizar(id, comarca) : this.api.criar(comarca));
      this.dialogAberto.set(false);
      this.mensagens.add({
        severity: 'success',
        summary: id ? 'Comarca atualizada.' : 'Comarca cadastrada.',
      });
      await this.carregar();
    } catch (e) {
      this.erro.set(mensagemDeErro(e));
    } finally {
      this.salvando.set(false);
    }
  }

  excluir(comarca: ComarcaResponse): void {
    this.confirmacao.confirm({
      header: 'Excluir comarca',
      message: `Deseja excluir a comarca ${comarca.nome}?`,
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: 'Excluir',
      rejectLabel: 'Cancelar',
      acceptButtonProps: { severity: 'danger' },
      rejectButtonProps: { severity: 'secondary', outlined: true },
      accept: async () => {
        try {
          await firstValueFrom(this.api.excluir(comarca.id));
          this.mensagens.add({ severity: 'success', summary: 'Comarca excluída.' });
          await this.carregar();
        } catch (e) {
          this.mensagens.add({ severity: 'error', summary: mensagemDeErro(e) });
        }
      },
    });
  }
}
