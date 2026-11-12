package br.edu.unimater.zela.importacaoextrato;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OfxTransacao(
        int posicao,
        String tipo,
        LocalDate data,
        BigDecimal valor,
        String fitid,
        String descricao,
        String numeroDocumento
) {}
