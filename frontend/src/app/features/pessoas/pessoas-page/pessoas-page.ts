import { DatePipe } from '@angular/common';
import { Component, DestroyRef, inject, signal } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import { ConfirmationService, MessageService } from 'primeng/api';
import { Button } from 'primeng/button';
import { ConfirmDialog } from 'primeng/confirmdialog';
import { DatePicker } from 'primeng/datepicker';
import { Dialog } from 'primeng/dialog';
import { IconField } from 'primeng/iconfield';
import { InputIcon } from 'primeng/inputicon';
import { InputMask } from 'primeng/inputmask';
import { InputText } from 'primeng/inputtext';
import { Message } from 'primeng/message';
import { TableModule } from 'primeng/table';
import { Textarea } from 'primeng/textarea';
import { Toast } from 'primeng/toast';
import { PessoaApi, PessoaRequest, PessoaResponse } from '../pessoa-api';
import { criarPessoaForm } from '../pessoa.form';
import { CpfPipe } from '../../../shared/pipes/cpf-pipe';
import { formatLocalDate, parseLocalDate } from '../../../shared/util/data';
import { mensagemDeErro } from '../../../shared/util/erro';

@Component({
  imports: [
    ReactiveFormsModule,
    DatePipe,
    CpfPipe,
    Button,
    ConfirmDialog,
    DatePicker,
    Dialog,
    IconField,
    InputIcon,
    InputMask,
    InputText,
    Message,
    TableModule,
    Textarea,
    Toast,
  ],
  providers: [ConfirmationService, MessageService],
  selector: 'app-pessoas-page',
  templateUrl: './pessoas-page.html',
})
export class PessoasPage {
  private readonly api = inject(PessoaApi);
  private readonly confirmacao = inject(ConfirmationService);
  private readonly mensagens = inject(MessageService);

  readonly pessoas = signal<PessoaResponse[]>([]);
  readonly carregando = signal(true);
  readonly erroLista = signal(false);

  readonly dialogAberto = signal(false);
  readonly editandoId = signal<string | null>(null);
  readonly salvando = signal(false);
  readonly erro = signal<string | null>(null);

  readonly hoje = new Date();
  readonly form = criarPessoaForm();

  private busca = '';
  private timerBusca?: ReturnType<typeof setTimeout>;
  private ultimaRequisicao = 0;

  constructor() {
    inject(DestroyRef).onDestroy(() => clearTimeout(this.timerBusca));
    void this.carregar();
  }

  get nome() {
    return this.form.controls.nome;
  }

  get dataNascimento() {
    return this.form.controls.dataNascimento;
  }

  async carregar(): Promise<void> {
    const requisicao = ++this.ultimaRequisicao;
    this.carregando.set(true);
    this.erroLista.set(false);

    try {
      const pessoas = await firstValueFrom(this.api.listar(this.busca));
      if (requisicao === this.ultimaRequisicao) {
        this.pessoas.set(pessoas);
      }
    } catch (e) {
      if (requisicao === this.ultimaRequisicao) {
        console.error('[pessoas] falha ao carregar a lista', e);
        this.erroLista.set(true);
      }
    } finally {
      if (requisicao === this.ultimaRequisicao) {
        this.carregando.set(false);
      }
    }
  }

  buscar(valor: string): void {
    clearTimeout(this.timerBusca);
    this.timerBusca = setTimeout(() => {
      this.busca = valor;
      void this.carregar();
    }, 300);
  }

  novo(): void {
    this.editandoId.set(null);
    this.erro.set(null);
    this.form.reset();
    this.dialogAberto.set(true);
  }

  editar(pessoa: PessoaResponse): void {
    this.editandoId.set(pessoa.id);
    this.erro.set(null);
    this.form.reset({
      nome: pessoa.nome,
      cpf: pessoa.cpf ?? '',
      dataNascimento: parseLocalDate(pessoa.dataNascimento),
      endereco: pessoa.endereco ?? '',
      observacao: pessoa.observacao ?? '',
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

    const { nome, cpf, dataNascimento, endereco, observacao } = this.form.getRawValue();
    const pessoa: PessoaRequest = {
      nome: nome.trim(),
      cpf: cpf || null,
      dataNascimento: formatLocalDate(dataNascimento!),
      endereco: endereco.trim() || null,
      observacao: observacao.trim() || null,
    };

    try {
      const id = this.editandoId();
      await firstValueFrom(id ? this.api.atualizar(id, pessoa) : this.api.criar(pessoa));
      this.dialogAberto.set(false);
      this.mensagens.add({
        severity: 'success',
        summary: id ? 'Pessoa atualizada.' : 'Pessoa cadastrada.',
      });
      await this.carregar();
    } catch (e) {
      this.erro.set(mensagemDeErro(e));
    } finally {
      this.salvando.set(false);
    }
  }

  excluir(pessoa: PessoaResponse): void {
    this.confirmacao.confirm({
      header: 'Excluir pessoa',
      message: `Deseja excluir ${pessoa.nome}?`,
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: 'Excluir',
      rejectLabel: 'Cancelar',
      acceptButtonProps: { severity: 'danger' },
      rejectButtonProps: { severity: 'secondary', outlined: true },
      accept: async () => {
        try {
          await firstValueFrom(this.api.excluir(pessoa.id));
          this.mensagens.add({ severity: 'success', summary: 'Pessoa excluída.' });
          await this.carregar();
        } catch (e) {
          this.mensagens.add({ severity: 'error', summary: mensagemDeErro(e) });
        }
      },
    });
  }
}
