package br.edu.unimater.zela.importacaoextrato;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record OfxExtrato(
        String bancoId,
        String agenciaId,
        String contaId,
        LocalDate dataInicio,
        LocalDate dataFim,
        BigDecimal saldoFinal,
        LocalDate dataSaldoFinal,
        List<OfxTransacao> transacoes
) {}
