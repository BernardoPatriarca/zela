package br.edu.unimater.zela.contabancaria.dto;

import br.edu.unimater.zela.contabancaria.Banco;
import br.edu.unimater.zela.contabancaria.ContaBancaria;
import br.edu.unimater.zela.contabancaria.TipoConta;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ContaBancariaResponseDto(
        UUID id,
        UUID casoId,
        Banco banco,
        TipoConta tipo,
        String agencia,
        String numero,
        String descricao,
        BigDecimal saldoInicial,
        LocalDate dataSaldoInicial,
        Instant criadoEm,
        Instant atualizadoEm
) {
    public static ContaBancariaResponseDto from(ContaBancaria conta) {
        return new ContaBancariaResponseDto(
                conta.id,
                conta.casoId,
                conta.banco,
                conta.tipo,
                conta.agencia,
                conta.numero,
                conta.descricao,
                conta.saldoInicial,
                conta.dataSaldoInicial,
                conta.criadoEm,
                conta.atualizadoEm
        );
    }
}
