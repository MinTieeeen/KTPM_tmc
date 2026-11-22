package com.tmc.central.employee.infrastructure;

import com.tmc.central.employee.api.EmployeeController;
import com.tmc.central.employee.api.QrTokenController;
import com.tmc.central.employee.domain.CredentialUnavailableException;
import com.tmc.central.employee.domain.EmployeeNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
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
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;
import java.util.Map;

/**
 * Lỗi của module employee dạng ProblemDetail có {@code code} (mục 6.8): {@code 404 EMPLOYEE_NOT_FOUND},
 * {@code 409 CREDENTIAL_REVOKED | NO_ACTIVE_CREDENTIAL | EMPLOYEE_INACTIVE | CONCURRENT_UPDATE},
 * {@code 400 VALIDATION_ERROR}.
 */
@RestControllerAdvice(assignableTypes = {EmployeeController.class, QrTokenController.class})
public class EmployeeExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(EmployeeExceptionHandler.class);

    @ExceptionHandler(EmployeeNotFoundException.class)
    ResponseEntity<ProblemDetail> handleNotFound(EmployeeNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "EMPLOYEE_NOT_FOUND", "Employee not found");
    }

    @ExceptionHandler(CredentialUnavailableException.class)
    ResponseEntity<ProblemDetail> handleUnavailable(CredentialUnavailableException ex) {
        return problem(HttpStatus.CONFLICT, ex.code(), ex.getMessage());
    }

    /** Vi phạm {@code ux_one_active} khi hai lệnh cấp chạy đồng thời. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ProblemDetail> handleConcurrentUpdate(DataIntegrityViolationException ex) {
        log.warn("Xung đột khi cập nhật credential: {}", ex.getMostSpecificCause().getMessage());
        return problem(HttpStatus.CONFLICT, "CONCURRENT_UPDATE", "The credential was changed concurrently");
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers, HttpStatusCode status,
                                                                  WebRequest request) {
        List<Map<String, String>> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> Map.of("field", e.getField(), "message", String.valueOf(e.getDefaultMessage())))
                .toList();
        ProblemDetail body = validation();
        body.setProperty("errors", errors);
        return ResponseEntity.badRequest().body(body);
    }

    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(HandlerMethodValidationException ex,
                                                                            HttpHeaders headers,
                                                                            HttpStatusCode status,
                                                                            WebRequest request) {
        return ResponseEntity.badRequest().body(validation());
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                  HttpHeaders headers, HttpStatusCode status,
                                                                  WebRequest request) {
        return ResponseEntity.badRequest().body(validation());
    }

    private static ResponseEntity<ProblemDetail> problem(HttpStatus status, String code, String detail) {
        ProblemDetail body = ProblemDetail.forStatusAndDetail(status, detail);
        body.setProperty("code", code);
        return ResponseEntity.status(status).body(body);
    }

    private static ProblemDetail validation() {
        ProblemDetail body = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Request validation failed");
        body.setProperty("code", "VALIDATION_ERROR");
        return body;
    }
}
