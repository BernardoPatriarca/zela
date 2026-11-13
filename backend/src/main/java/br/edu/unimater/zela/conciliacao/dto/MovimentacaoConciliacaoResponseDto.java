package br.edu.unimater.zela.conciliacao.dto;

import br.edu.unimater.zela.categoria.TipoCategoria;
import br.edu.unimater.zela.importacaoextrato.FormaConciliacao;
import br.edu.unimater.zela.importacaoextrato.MovimentacaoExtrato;
import br.edu.unimater.zela.importacaoextrato.SituacaoMovimentacao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record MovimentacaoConciliacaoResponseDto(
        UUID id,
        UUID contaBancariaId,
        LocalDate dataMovimentacao,
        String descricao,
        TipoCategoria tipo,
        BigDecimal valor,
        SituacaoMovimentacao situacao,
        FormaConciliacao formaConciliacao,
        UUID lancamentoId,
        String lancamentoDescricao,
        String lancamentoCategoriaNome,
        int candidatos
) {
    public static MovimentacaoConciliacaoResponseDto from(MovimentacaoExtrato movimentacao, int candidatos) {
        return new MovimentacaoConciliacaoResponseDto(
                movimentacao.id,
                movimentacao.contaBancaria.id,
                movimentacao.dataMovimentacao,
                movimentacao.descricao,
                movimentacao.tipo,
                movimentacao.valor,
                movimentacao.situacao,
                movimentacao.formaConciliacao,
                movimentacao.lancamento == null ? null : movimentacao.lancamento.id,
                movimentacao.lancamento == null ? null : movimentacao.lancamento.descricao,
                movimentacao.lancamento == null ? null : movimentacao.lancamento.categoria.nome,
                candidatos
        );
    }
}
