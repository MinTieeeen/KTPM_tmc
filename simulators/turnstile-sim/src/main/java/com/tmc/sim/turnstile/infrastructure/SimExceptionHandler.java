package com.tmc.sim.turnstile.infrastructure;

import com.tmc.sim.turnstile.domain.SimulatedFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/** {@code SimulatedFailureException(ERROR)} → 500, {@code (TIMEOUT)} → 504; dữ liệu sai → {@code 400 VALIDATION_ERROR}. */
@RestControllerAdvice
public class SimExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(SimulatedFailureException.class)
    ResponseEntity<ProblemDetail> handleSimulated(SimulatedFailureException ex) {
        HttpStatus status = ex.kind() == SimulatedFailureException.Kind.TIMEOUT
                ? HttpStatus.GATEWAY_TIMEOUT : HttpStatus.INTERNAL_SERVER_ERROR;
        return problem(status, "SIMULATED_" + ex.kind().name(), ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ProblemDetail> handleInvalid(IllegalArgumentException ex) {
        return problem(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", ex.getMessage());
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                  HttpHeaders headers, HttpStatusCode status,
                                                                  WebRequest request) {
        ProblemDetail body = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Request body is not readable");
        body.setProperty("code", "VALIDATION_ERROR");
        return ResponseEntity.badRequest().body(body);
    }

    private static ResponseEntity<ProblemDetail> problem(HttpStatus status, String code, String detail) {
        ProblemDetail body = ProblemDetail.forStatusAndDetail(status, detail);
        body.setProperty("code", code);
        return ResponseEntity.status(status).body(body);
    }
}
