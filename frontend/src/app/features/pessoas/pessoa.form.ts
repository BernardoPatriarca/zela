import { inject } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';

export function criarPessoaForm() {
  const fb = inject(FormBuilder);

  return fb.nonNullable.group({
    nome: ['', [Validators.required, Validators.maxLength(150)]],
    cpf: [''],
    dataNascimento: fb.control<Date | null>(null, Validators.required),
    endereco: ['', [Validators.maxLength(255)]],
    observacao: ['', [Validators.maxLength(1000)]],
  });
}
