package br.edu.unimater.zela.caso.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;

public record CasoEncerramentoDto(
        @NotNull(message = "A data de encerramento é obrigatória.")
        @PastOrPresent(message = "A data de encerramento não pode ser futura.")
        LocalDate data
) {}
