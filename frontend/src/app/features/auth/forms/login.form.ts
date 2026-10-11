import { inject } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';

export function criarLoginForm() {
  const fb = inject(FormBuilder);

  return fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email, Validators.maxLength(255)]],
    senha: ['', [Validators.required]],
  });
}
