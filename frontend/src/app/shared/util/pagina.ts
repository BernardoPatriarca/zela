export interface Pagina<T> {
  itens: T[];
  total: number;
  pagina: number;
  tamanho: number;
}

export interface Opcao {
  id: string;
  nome: string;
}

export const ITENS_POR_PAGINA = 10;

export function inicioDaUltimaPagina(total: number, linhas: number): number {
  return Math.max(0, Math.floor((total - 1) / linhas) * linhas);
}
