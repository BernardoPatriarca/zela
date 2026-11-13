package br.edu.unimater.zela.conciliacao;

import br.edu.unimater.zela.categoria.TipoCategoria;
import br.edu.unimater.zela.importacaoextrato.MovimentacaoExtrato;
import br.edu.unimater.zela.lancamento.Lancamento;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.UUID;

public record ChaveConciliacao(
        UUID contaBancariaId,
        TipoCategoria tipo,
        BigDecimal valor,
        LocalDate data
) {
    public ChaveConciliacao {
        valor = valor.setScale(2, RoundingMode.HALF_UP);
    }

    public static ChaveConciliacao de(MovimentacaoExtrato movimentacao) {
        return new ChaveConciliacao(movimentacao.contaBancaria.id, movimentacao.tipo, movimentacao.valor,
                movimentacao.dataMovimentacao);
    }

    public static ChaveConciliacao de(Lancamento lancamento) {
        return new ChaveConciliacao(lancamento.contaBancaria.id, lancamento.tipo, lancamento.valor,
                lancamento.dataLancamento);
    }
}
