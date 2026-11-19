package br.edu.unimater.zela.prestacaocontas;

public enum TipoPendencia {
    MOVIMENTACAO_PENDENTE,
    LANCAMENTO_SEM_EXTRATO,
    EXTRATO_FALTANDO,
    SALDO_DIVERGENTE,
    SALDO_INICIAL_DIFERENTE,
    POSSIVEL_DUPLICIDADE
}
