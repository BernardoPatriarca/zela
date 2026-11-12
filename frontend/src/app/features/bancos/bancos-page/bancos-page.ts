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
import { TableLazyLoadEvent, TableModule } from 'primeng/table';
import { Tag } from 'primeng/tag';
import { Toast } from 'primeng/toast';
import { BancoApi, BancoRequest, BancoResponse } from '../banco-api';
import { criarBancoForm } from '../banco.form';
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
    TableModule,
    Tag,
    Toast,
  ],
  providers: [ConfirmationService, MessageService],
  selector: 'app-bancos-page',
  templateUrl: './bancos-page.html',
})
export class BancosPage {
  private readonly api = inject(BancoApi);
  private readonly confirmacao = inject(ConfirmationService);
  private readonly mensagens = inject(MessageService);

  readonly bancos = signal<BancoResponse[]>([]);
  readonly total = signal(0);
  readonly primeiro = signal(0);
  readonly linhas = ITENS_POR_PAGINA;
  readonly carregando = signal(true);
  readonly erroLista = signal(false);

  readonly dialogAberto = signal(false);
  readonly editandoId = signal<string | null>(null);
  readonly salvando = signal(false);
  readonly erro = signal<string | null>(null);

  readonly form = criarBancoForm();

  private busca = '';
  private timerBusca?: ReturnType<typeof setTimeout>;
  private ultimaRequisicao = 0;

  constructor() {
    inject(DestroyRef).onDestroy(() => clearTimeout(this.timerBusca));
  }

  get codigo() {
    return this.form.controls.codigo;
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
        this.bancos.set(pagina.itens);
        this.total.set(pagina.total);
      }
    } catch (e) {
      if (requisicao === this.ultimaRequisicao) {
        console.error('[bancos] falha ao carregar a lista', e);
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

  novo(): void {
    this.editandoId.set(null);
    this.erro.set(null);
    this.form.reset();
    this.dialogAberto.set(true);
  }

  editar(banco: BancoResponse): void {
    this.editandoId.set(banco.id);
    this.erro.set(null);
    this.form.reset({ codigo: banco.codigo, nome: banco.nome });
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

    const { codigo, nome } = this.form.getRawValue();
    const banco: BancoRequest = { codigo, nome: nome.trim() };

    try {
      const id = this.editandoId();
      await firstValueFrom(id ? this.api.atualizar(id, banco) : this.api.criar(banco));
      this.dialogAberto.set(false);
      this.mensagens.add({
        severity: 'success',
        summary: id ? 'Banco atualizado.' : 'Banco cadastrado.',
      });
      await this.carregar();
    } catch (e) {
      this.erro.set(mensagemDeErro(e));
    } finally {
      this.salvando.set(false);
    }
  }

  excluir(banco: BancoResponse): void {
    this.confirmacao.confirm({
      header: 'Excluir banco',
      message: `Deseja excluir o banco ${banco.codigo} - ${banco.nome}?`,
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: 'Excluir',
      rejectLabel: 'Cancelar',
      acceptButtonProps: { severity: 'danger' },
      rejectButtonProps: { severity: 'secondary', outlined: true },
      accept: async () => {
        try {
          await firstValueFrom(this.api.excluir(banco.id));
          this.mensagens.add({ severity: 'success', summary: 'Banco excluído.' });
          await this.carregar();
        } catch (e) {
          this.mensagens.add({ severity: 'error', summary: mensagemDeErro(e) });
        }
      },
    });
  }
}
