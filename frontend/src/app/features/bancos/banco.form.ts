import { inject } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';

const CODIGO_BANCO = /^[0-9]{3}$/;

export function criarBancoForm() {
  const fb = inject(FormBuilder);

  return fb.nonNullable.group({
    codigo: ['', [Validators.required, Validators.pattern(CODIGO_BANCO)]],
    nome: ['', [Validators.required, Validators.maxLength(100)]],
  });
}
