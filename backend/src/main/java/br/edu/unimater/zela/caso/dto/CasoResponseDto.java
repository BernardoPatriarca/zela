package br.edu.unimater.zela.caso.dto;

import br.edu.unimater.zela.caso.Caso;
import br.edu.unimater.zela.caso.PeriodoPrestacao;
import br.edu.unimater.zela.caso.SituacaoCaso;
import br.edu.unimater.zela.caso.TipoCaso;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record CasoResponseDto(
        UUID id,
        TipoCaso tipo,
        String numeroProcesso,
        UUID varaId,
        String varaNome,
        UUID comarcaId,
        String comarcaNome,
        UUID pessoaProtegidaId,
        String pessoaProtegidaNome,
        UUID responsavelId,
        String responsavelNome,
        int periodicidadeMeses,
        int prazoEntregaDias,
        SituacaoCaso situacao,
        LocalDate encerradoEm,
        PeriodoPrestacaoResponseDto periodoAtual,
        Instant criadoEm,
        Instant atualizadoEm
) {
    public static CasoResponseDto from(Caso caso, PeriodoPrestacao periodoAtual) {
        return new CasoResponseDto(
                caso.id,
                caso.tipo,
                caso.numeroProcesso,
                caso.vara.id,
                caso.vara.nome,
                caso.vara.comarca.id,
                caso.vara.comarca.nome,
                caso.pessoaProtegida.id,
                caso.pessoaProtegida.nome,
                caso.responsavel.id,
                caso.responsavel.nome,
                caso.periodicidadeMeses,
                caso.prazoEntregaDias,
                caso.getSituacao(),
                caso.encerradoEm,
                PeriodoPrestacaoResponseDto.from(periodoAtual),
                caso.criadoEm,
                caso.atualizadoEm
        );
    }
}
