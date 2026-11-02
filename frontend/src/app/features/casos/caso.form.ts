import { inject } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { SituacaoCaso, TipoCaso } from './caso-api';

export const TIPOS_CASO: { label: string; value: TipoCaso }[] = [
  { label: 'Curatela', value: 'CURATELA' },
  { label: 'Tutela', value: 'TUTELA' },
];

export const SITUACOES_CASO: { label: string; value: SituacaoCaso }[] = [
  { label: 'Ativos', value: 'ATIVO' },
  { label: 'Encerrados', value: 'ENCERRADO' },
];

export function criarCasoForm() {
  const fb = inject(FormBuilder);

  return fb.nonNullable.group({
    tipo: fb.nonNullable.control<TipoCaso>('CURATELA', Validators.required),
    numeroProcesso: ['', [Validators.required, Validators.minLength(20)]],
    comarcaId: fb.control<string | null>(null, Validators.required),
    varaId: fb.control<string | null>(null, Validators.required),
    pessoaProtegidaId: fb.control<string | null>(null, Validators.required),
    responsavelId: fb.control<string | null>(null, Validators.required),
    periodicidadeMeses: fb.control<number | null>(12, [
      Validators.required,
      Validators.min(1),
      Validators.max(60),
    ]),
    prazoEntregaDias: fb.control<number | null>(30, [
      Validators.required,
      Validators.min(0),
      Validators.max(365),
    ]),
    inicioPeriodo: fb.control<Date | null>(null, Validators.required),
  });
}
