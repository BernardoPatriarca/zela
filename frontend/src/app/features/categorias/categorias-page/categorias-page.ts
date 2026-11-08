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
import { CategoriaApi, CategoriaResponse, TipoCategoria } from '../categoria-api';
import { criarCategoriaForm, TIPOS_CATEGORIA } from '../categoria.form';
import { mensagemDeErro } from '../../../shared/util/erro';
import { inicioDaUltimaPagina, ITENS_POR_PAGINA } from '../../../shared/util/pagina';

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
  selector: 'app-categorias-page',
  templateUrl: './categorias-page.html',
})
export class CategoriasPage {
  private readonly api = inject(CategoriaApi);
  private readonly confirmacao = inject(ConfirmationService);
  private readonly mensagens = inject(MessageService);

  readonly categorias = signal<CategoriaResponse[]>([]);
  readonly total = signal(0);
  readonly primeiro = signal(0);
  readonly linhas = ITENS_POR_PAGINA;
  readonly carregando = signal(true);
  readonly erroLista = signal(false);

  readonly dialogAberto = signal(false);
  readonly editandoId = signal<string | null>(null);
  readonly salvando = signal(false);
  readonly erro = signal<string | null>(null);

  readonly tipos = TIPOS_CATEGORIA;
  readonly form = criarCategoriaForm();

  tipoFiltro: TipoCategoria | null = null;
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
        this.api.listar(this.tipoFiltro, this.busca, this.primeiro() / this.linhas, this.linhas),
      );
      if (requisicao === this.ultimaRequisicao) {
        if (pagina.itens.length === 0 && this.primeiro() > 0) {
          this.primeiro.set(inicioDaUltimaPagina(pagina.total, this.linhas));
          void this.carregar();
          return;
        }
        this.categorias.set(pagina.itens);
        this.total.set(pagina.total);
      }
    } catch (e) {
      if (requisicao === this.ultimaRequisicao) {
        console.error('[categorias] falha ao carregar a lista', e);
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

  filtrarPorTipo(tipo: TipoCategoria | null): void {
    this.tipoFiltro = tipo;
    this.primeiro.set(0);
    void this.carregar();
  }

  nomeTipo(tipo: TipoCategoria): string {
    return this.tipos.find((opcao) => opcao.value === tipo)?.label ?? tipo;
  }

  nova(): void {
    this.editandoId.set(null);
    this.erro.set(null);
    this.form.reset({ tipo: this.tipoFiltro ?? 'DESPESA', nome: '' });
    this.form.controls.tipo.enable();
    this.dialogAberto.set(true);
  }

  editar(categoria: CategoriaResponse): void {
    this.editandoId.set(categoria.id);
    this.erro.set(null);
    this.form.reset({ tipo: categoria.tipo, nome: categoria.nome });
    this.form.controls.tipo.disable();
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

    const { tipo, nome } = this.form.getRawValue();

    try {
      const id = this.editandoId();
      await firstValueFrom(
        id
          ? this.api.atualizar(id, { nome: nome.trim() })
          : this.api.criar({ tipo, nome: nome.trim() }),
      );
      this.dialogAberto.set(false);
      this.mensagens.add({
        severity: 'success',
        summary: id ? 'Categoria atualizada.' : 'Categoria cadastrada.',
      });
      await this.carregar();
    } catch (e) {
      this.erro.set(mensagemDeErro(e));
    } finally {
      this.salvando.set(false);
    }
  }

  excluir(categoria: CategoriaResponse): void {
    this.confirmacao.confirm({
      header: 'Excluir categoria',
      message: `Deseja excluir a categoria ${categoria.nome}?`,
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: 'Excluir',
      rejectLabel: 'Cancelar',
      acceptButtonProps: { severity: 'danger' },
      rejectButtonProps: { severity: 'secondary', outlined: true },
      accept: async () => {
        try {
          await firstValueFrom(this.api.excluir(categoria.id));
          this.mensagens.add({ severity: 'success', summary: 'Categoria excluída.' });
          await this.carregar();
        } catch (e) {
          this.mensagens.add({ severity: 'error', summary: mensagemDeErro(e) });
        }
      },
    });
  }
}
