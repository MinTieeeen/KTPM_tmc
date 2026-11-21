package com.tmc.central.ingestion.infrastructure;

import com.tmc.central.ingestion.api.SyncController;
import com.tmc.central.ingestion.domain.SyncRejectedException;
import com.tmc.central.station.api.UnknownGateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

/**
 * Lỗi của SyncAPI dạng ProblemDetail có {@code code} (mục 6.7): {@code 400 UNSUPPORTED_CONTRACT_VERSION |
 * BATCH_TOO_LARGE | VALIDATION_ERROR}, {@code 403 GATE_MISMATCH | UNKNOWN_GATE}.
 */
@RestControllerAdvice(assignableTypes = SyncController.class)
public class IngestionExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(IngestionExceptionHandler.class);

    @ExceptionHandler(SyncRejectedException.class)
    ResponseEntity<ProblemDetail> handleRejected(SyncRejectedException ex) {
        HttpStatus status = switch (ex.reason()) {
            case UNSUPPORTED_CONTRACT_VERSION, BATCH_TOO_LARGE -> HttpStatus.BAD_REQUEST;
            case GATE_MISMATCH, UNKNOWN_GATE -> HttpStatus.FORBIDDEN;
        };
        log.warn("Từ chối yêu cầu đồng bộ: {} – {}", ex.reason(), ex.getMessage());
        return problem(status, ex.reason().name(), ex.getMessage());
    }

    @ExceptionHandler(UnknownGateException.class)
    ResponseEntity<ProblemDetail> handleUnknownGate(UnknownGateException ex) {
        return problem(HttpStatus.FORBIDDEN, SyncRejectedException.Reason.UNKNOWN_GATE.name(), ex.getMessage());
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
