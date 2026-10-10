import { inject } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { senhasIguais } from '../validators/senhas-iguais.validator';

export function criarRegisterForm() {
  const fb = inject(FormBuilder);

  return fb.nonNullable.group(
    {
      nome: ['', [Validators.required, Validators.maxLength(150)]],
      email: ['', [Validators.required, Validators.email, Validators.maxLength(255)]],
      senha: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(72)]],
      confirmacao: ['', [Validators.required]],
    },
    { validators: senhasIguais },
  );
}
