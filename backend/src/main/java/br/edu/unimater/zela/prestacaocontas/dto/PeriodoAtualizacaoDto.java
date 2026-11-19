package br.edu.unimater.zela.prestacaocontas.dto;

import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record PeriodoAtualizacaoDto(
        LocalDate prazoCurador,

        @Size(max = 4000, message = "As notas explicativas devem ter no máximo 4000 caracteres.")
        String notasExplicativas
) {}
