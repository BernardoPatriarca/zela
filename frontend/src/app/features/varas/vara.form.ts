import { inject } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';

export function criarVaraForm() {
  const fb = inject(FormBuilder);

  return fb.nonNullable.group({
    comarcaId: fb.control<string | null>(null, Validators.required),
    nome: ['', [Validators.required, Validators.maxLength(150)]],
  });
}
