import { FormBuilder, Validators } from '@angular/forms';
import { inject } from '@angular/core';
import { SituacaoPeriodo, TipoPendencia } from './prestacao-contas-api';

export const SITUACOES_PERIODO: { label: string; value: SituacaoPeriodo }[] = [
  { label: 'Aguardando curador', value: 'AGUARDANDO_CURADOR' },
  { label: 'Em revisão', value: 'EM_REVISAO' },
  { label: 'Pronto', value: 'PRONTO' },
  { label: 'Entregue', value: 'ENTREGUE' },
];

export const NOMES_PENDENCIA: Record<TipoPendencia, string> = {
  MOVIMENTACAO_PENDENTE: 'Movimentação pendente',
  LANCAMENTO_SEM_EXTRATO: 'Lançamento sem extrato',
  EXTRATO_FALTANDO: 'Extrato faltando',
  SALDO_DIVERGENTE: 'Saldo divergente',
  SALDO_INICIAL_DIFERENTE: 'Saldo inicial diferente',
  POSSIVEL_DUPLICIDADE: 'Possível duplicidade',
};

export function criarDadosProfissionaisForm() {
  const fb = inject(FormBuilder);

  return fb.nonNullable.group({
    nomeCompleto: ['', [Validators.required, Validators.maxLength(150)]],
    oabNumero: ['', [Validators.required, Validators.pattern(/^\d{1,7}$/)]],
    oabUf: ['', [Validators.required, Validators.pattern(/^[A-Za-z]{2}$/)]],
  });
}
