import { Pipe, PipeTransform } from '@angular/core';

@Pipe({ name: 'cnj' })
export class CnjPipe implements PipeTransform {
  transform(numero: string | null): string {
    return numero
      ? numero.replace(/(\d{7})(\d{2})(\d{4})(\d)(\d{2})(\d{4})/, '$1-$2.$3.$4.$5.$6')
      : '-';
  }
}
