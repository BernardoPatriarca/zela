package br.edu.unimater.zela.shared.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(int status, String message, List<FieldError> fields) {
    public record FieldError(String field, String message) {}
}