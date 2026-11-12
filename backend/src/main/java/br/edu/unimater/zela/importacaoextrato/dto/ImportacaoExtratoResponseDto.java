package br.edu.unimater.zela.importacaoextrato.dto;

import br.edu.unimater.zela.importacaoextrato.ImportacaoExtrato;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ImportacaoExtratoResponseDto(
        UUID id,
        UUID contaBancariaId,
        String nomeArquivo,
        int tamanhoBytes,
        LocalDate dataInicio,
        LocalDate dataFim,
        BigDecimal saldoFinal,
        LocalDate dataSaldoFinal,
        int total,
        int importadas,
        int duplicadas,
        int ignoradas,
        Instant criadoEm
) {
    public static ImportacaoExtratoResponseDto from(ImportacaoExtrato importacao) {
        return new ImportacaoExtratoResponseDto(
                importacao.id,
                importacao.contaBancaria.id,
                importacao.nomeArquivo,
                importacao.tamanhoBytes,
                importacao.dataInicio,
                importacao.dataFim,
                importacao.saldoFinal,
                importacao.dataSaldoFinal,
                importacao.total,
                importacao.importadas,
                importacao.duplicadas,
                importacao.ignoradas,
                importacao.criadoEm
        );
    }
}
