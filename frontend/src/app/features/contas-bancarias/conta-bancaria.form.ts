import { inject } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { Banco, TipoConta } from './conta-bancaria-api';

export const BANCOS: { label: string; value: Banco }[] = [
  { label: '001 - Banco do Brasil', value: 'BANCO_DO_BRASIL' },
  { label: '041 - Banrisul', value: 'BANRISUL' },
  { label: '237 - Bradesco', value: 'BRADESCO' },
  { label: '336 - C6 Bank', value: 'C6' },
  { label: '104 - Caixa Econômica Federal', value: 'CAIXA' },
  { label: '133 - Cresol', value: 'CRESOL' },
  { label: '077 - Inter', value: 'INTER' },
  { label: '341 - Itaú', value: 'ITAU' },
  { label: '323 - Mercado Pago', value: 'MERCADO_PAGO' },
  { label: '260 - Nubank', value: 'NUBANK' },
  { label: '033 - Santander', value: 'SANTANDER' },
  { label: '756 - Sicoob', value: 'SICOOB' },
  { label: '748 - Sicredi', value: 'SICREDI' },
  { label: 'Outro', value: 'OUTRO' },
];

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
    banco: fb.control<Banco | null>(null, Validators.required),
    tipo: fb.nonNullable.control<TipoConta>('CORRENTE', Validators.required),
    agencia: ['', [Validators.required, Validators.pattern(AGENCIA)]],
    numero: ['', [Validators.required, Validators.pattern(NUMERO_CONTA)]],
    descricao: ['', [Validators.maxLength(100)]],
    saldoInicial: fb.control<number | null>(null, Validators.required),
    dataSaldoInicial: fb.control<Date | null>(null, Validators.required),
  });
}
