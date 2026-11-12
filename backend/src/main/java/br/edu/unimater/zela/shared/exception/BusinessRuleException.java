package br.edu.unimater.zela.shared.exception;

import java.util.List;

public class BusinessRuleException extends RuntimeException {

    private final List<ErrorResponse.FieldError> campos;

    public BusinessRuleException(String message) {
        this(message, null);
    }

    public BusinessRuleException(String message, List<ErrorResponse.FieldError> campos) {
        super(message);
        this.campos = campos;
    }

    public List<ErrorResponse.FieldError> getCampos() {
        return campos;
    }
}
