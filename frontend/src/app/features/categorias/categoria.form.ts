import { inject } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { TipoCategoria } from './categoria-api';

export const TIPOS_CATEGORIA: { label: string; value: TipoCategoria }[] = [
  { label: 'Receita', value: 'RECEITA' },
  { label: 'Despesa', value: 'DESPESA' },
];

export function criarCategoriaForm() {
  const fb = inject(FormBuilder);

  return fb.nonNullable.group({
    tipo: fb.nonNullable.control<TipoCategoria>('DESPESA', Validators.required),
    nome: ['', [Validators.required, Validators.maxLength(100)]],
  });
}
