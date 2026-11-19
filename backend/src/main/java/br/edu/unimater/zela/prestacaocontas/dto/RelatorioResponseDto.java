package br.edu.unimater.zela.prestacaocontas.dto;

import br.edu.unimater.zela.prestacaocontas.RelatorioPrestacao;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record RelatorioResponseDto(
        UUID id,
        int versao,
        Instant geradoEm,
        boolean comRessalvas,
        int quantidadePendencias,
        BigDecimal saldoFinalTotal,
        int tamanhoBytes,
        String hashSha256
) {
    public static RelatorioResponseDto from(RelatorioPrestacao relatorio) {
        return new RelatorioResponseDto(
                relatorio.id,
                relatorio.versao,
                relatorio.geradoEm,
                relatorio.comRessalvas,
                relatorio.quantidadePendencias,
                relatorio.saldoFinalTotal,
                relatorio.tamanhoBytes,
                relatorio.hashSha256
        );
    }
}
