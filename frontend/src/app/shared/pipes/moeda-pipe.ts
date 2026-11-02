import { Pipe, PipeTransform } from '@angular/core';

const formato = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' });

@Pipe({ name: 'moeda' })
export class MoedaPipe implements PipeTransform {
  transform(valor: number | null): string {
    return valor === null ? '-' : formato.format(valor);
  }
}
