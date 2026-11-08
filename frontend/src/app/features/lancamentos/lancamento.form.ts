import { inject } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { TipoCategoria } from '../categorias/categoria-api';
import { SituacaoLancamento } from './lancamento-api';

export const TIPOS_LANCAMENTO: { label: string; value: TipoCategoria }[] = [
  { label: 'Receita', value: 'RECEITA' },
  { label: 'Despesa', value: 'DESPESA' },
];

export const SITUACOES_LANCAMENTO: { label: string; value: SituacaoLancamento }[] = [
  { label: 'Pendente', value: 'PENDENTE' },
  { label: 'Conciliado', value: 'CONCILIADO' },
];

export function criarLancamentoForm() {
  const fb = inject(FormBuilder);

  return fb.nonNullable.group({
    tipo: fb.nonNullable.control<TipoCategoria>('DESPESA', Validators.required),
    dataLancamento: fb.control<Date | null>(null, Validators.required),
    valor: fb.control<number | null>(null, [Validators.required, Validators.min(0.01)]),
    contaBancariaId: fb.control<string | null>(null, Validators.required),
    categoriaId: fb.control<string | null>(null, Validators.required),
    descricao: ['', [Validators.required, Validators.maxLength(255)]],
    justificativa: ['', [Validators.required, Validators.minLength(10), Validators.maxLength(500)]],
  });
}
