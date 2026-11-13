package br.edu.unimater.zela.lancamento.dto;

import br.edu.unimater.zela.categoria.TipoCategoria;
import br.edu.unimater.zela.lancamento.Lancamento;
import br.edu.unimater.zela.lancamento.SituacaoLancamento;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record LancamentoResponseDto(
        UUID id,
        UUID contaBancariaId,
        UUID categoriaId,
        String categoriaNome,
        TipoCategoria tipo,
        LocalDate dataLancamento,
        BigDecimal valor,
        String descricao,
        SituacaoLancamento situacao,
        Instant criadoEm,
        Instant atualizadoEm
) {
    public static LancamentoResponseDto from(Lancamento lancamento, boolean conciliado) {
        return new LancamentoResponseDto(
                lancamento.id,
                lancamento.contaBancaria.id,
                lancamento.categoria.id,
                lancamento.categoria.nome,
                lancamento.tipo,
                lancamento.dataLancamento,
                lancamento.valor,
                lancamento.descricao,
                conciliado ? SituacaoLancamento.CONCILIADO : SituacaoLancamento.PENDENTE,
                lancamento.criadoEm,
                lancamento.atualizadoEm
        );
    }
}
