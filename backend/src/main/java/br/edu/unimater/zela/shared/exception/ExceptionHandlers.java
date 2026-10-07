package br.edu.unimater.zela.shared.exception;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import jakarta.ws.rs.WebApplicationException;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import java.util.List;

public class ExceptionHandlers {

    private static final Logger LOG = Logger.getLogger(ExceptionHandlers.class);

    @ServerExceptionMapper
    public RestResponse<ErrorResponse> notFound(ResourceNotFoundException e) {
        return build(404, e.getMessage(), null);
    }

    @ServerExceptionMapper
    public RestResponse<ErrorResponse> conflict(ConflictException e) {
        return build(409, e.getMessage(), null);
    }

    @ServerExceptionMapper
    public RestResponse<ErrorResponse> businessRule(BusinessRuleException e) {
        return build(422, e.getMessage(), null);
    }

    @ServerExceptionMapper
    public RestResponse<ErrorResponse> validation(ConstraintViolationException e) {
        var fields = e.getConstraintViolations().stream()
                .map(v -> {
                    String name = null;
                    for (Path.Node n : v.getPropertyPath()) name = n.getName();
                    return new ErrorResponse.FieldError(name, v.getMessage());
                })
                .toList();
        return build(400, "Dados inválidos", fields);
    }

    @ServerExceptionMapper
    public RestResponse<ErrorResponse> unexpected(Throwable e) {
        if (e instanceof WebApplicationException w) {
            int status = w.getResponse().getStatus();
            return build(status, w.getMessage(), null);
        }
        LOG.error("Erro não tratado", e);
        return build(500, "Erro interno. Tente novamente mais tarde.", null);
    }

    private RestResponse<ErrorResponse> build(int status, String message, List<ErrorResponse.FieldError> fields) {
        return RestResponse.ResponseBuilder
                .<ErrorResponse>create(status)
                .entity(new ErrorResponse(status, message, fields))
                .build();
    }
}