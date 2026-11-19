package br.edu.unimater.zela.prestacaocontas.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;

public record EntregaRequestDto(
        @NotNull(message = "A data de entrega é obrigatória.")
        @PastOrPresent(message = "A data de entrega não pode ser futura.")
        LocalDate dataEntrega
) {}
