import { inject } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { TipoConta } from './conta-bancaria-api';

export const TIPOS_CONTA: { label: string; value: TipoConta }[] = [
  { label: 'Corrente', value: 'CORRENTE' },
  { label: 'Poupança', value: 'POUPANCA' },
  { label: 'Pagamento', value: 'PAGAMENTO' },
];

const AGENCIA = /^[0-9]{1,8}(-[0-9Xx])?$/;
const NUMERO_CONTA = /^[0-9]{1,18}(-[0-9Xx])?$/;

export function criarContaBancariaForm() {
  const fb = inject(FormBuilder);

  return fb.nonNullable.group({
    bancoId: fb.control<string | null>(null, Validators.required),
    tipo: fb.nonNullable.control<TipoConta>('CORRENTE', Validators.required),
    agencia: ['', [Validators.required, Validators.pattern(AGENCIA)]],
    numero: ['', [Validators.required, Validators.pattern(NUMERO_CONTA)]],
    descricao: ['', [Validators.maxLength(100)]],
    saldoInicial: fb.control<number | null>(null, Validators.required),
    dataSaldoInicial: fb.control<Date | null>(null, Validators.required),
  });
}
