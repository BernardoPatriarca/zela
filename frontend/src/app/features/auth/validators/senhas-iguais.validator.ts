import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

export const senhasIguais: ValidatorFn = (grupo: AbstractControl): ValidationErrors | null => {
  const senha = grupo.get('senha')?.value;
  const confirmacao = grupo.get('confirmacao')?.value;

  if (senha !== confirmacao) {
    return { senhasDiferentes: true };
  }

  return null;
};
