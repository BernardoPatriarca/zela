import { inject } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';

export const UFS = [
  'AC',
  'AL',
  'AM',
  'AP',
  'BA',
  'CE',
  'DF',
  'ES',
  'GO',
  'MA',
  'MG',
  'MS',
  'MT',
  'PA',
  'PB',
  'PE',
  'PI',
  'PR',
  'RJ',
  'RN',
  'RO',
  'RR',
  'RS',
  'SC',
  'SE',
  'SP',
  'TO',
];

export function criarComarcaForm() {
  const fb = inject(FormBuilder);

  return fb.nonNullable.group({
    nome: ['', [Validators.required, Validators.maxLength(100)]],
    uf: ['PR', Validators.required],
  });
}
