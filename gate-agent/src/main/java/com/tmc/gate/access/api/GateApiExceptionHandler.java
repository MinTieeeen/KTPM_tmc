package com.tmc.gate.access.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;
import java.util.Map;

/**
 * Ánh xạ lỗi sang ProblemDetail theo hợp đồng {@code docs/api/gate-agent.md}:
 * {@code 400 VALIDATION_ERROR} khi body sai, {@code 500 LOCAL_STORE_ERROR} khi không lưu được lượt quét
 * (kiosk hiển thị "Không xác định" – không bao giờ báo xanh, INV-05).
 */
@RestControllerAdvice
public class GateApiExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GateApiExceptionHandler.class);

    static final String VALIDATION_ERROR = "VALIDATION_ERROR";
    static final String LOCAL_STORE_ERROR = "LOCAL_STORE_ERROR";

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers, HttpStatusCode status,
                                                                  WebRequest request) {
        List<Map<String, String>> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> Map.of("field", e.getField(), "message", String.valueOf(e.getDefaultMessage())))
                .toList();
        ProblemDetail body = validationProblem();
        body.setProperty("errors", errors);
        return ResponseEntity.badRequest().body(body);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                  HttpHeaders headers, HttpStatusCode status,
                                                                  WebRequest request) {
        return ResponseEntity.badRequest().body(validationProblem());
    }

    /** Lỗi SQLite hoặc chuyển trạng thái lượt quét không hợp lệ. */
    @ExceptionHandler({DataAccessException.class, IllegalStateException.class})
    ResponseEntity<ProblemDetail> handleLocalStore(RuntimeException ex) {
        log.error("Lỗi lưu trữ cục bộ", ex);
        ProblemDetail body = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                "Không lưu được dữ liệu cục bộ");
        body.setProperty("code", LOCAL_STORE_ERROR);
        return ResponseEntity.internalServerError().body(body);
    }

    private static ProblemDetail validationProblem() {
        ProblemDetail body = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Request validation failed");
        body.setProperty("code", VALIDATION_ERROR);
        return body;
    }
}
