package net.microfalx.bootstrap.restapi;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

import static net.microfalx.lang.ExceptionUtils.getRootCauseDescription;

@RestControllerAdvice(assignableTypes = RestApiController.class)
@Slf4j
public class RestApiExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<RestApiError> handleNotFound(EntityNotFoundException exception, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, exception.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler({IllegalArgumentException.class})
    public ResponseEntity<RestApiError> handleBadRequest(IllegalArgumentException exception, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, exception.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<RestApiError> handleUnreadable(HttpMessageNotReadableException exception, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Malformed JSON request", request.getRequestURI(), getDetails(exception));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RestApiError> handleValidation(MethodArgumentNotValidException exception, HttpServletRequest request) {
        Map<String, Object> details = new HashMap<>();
        for (var error : exception.getBindingResult().getFieldErrors()) {
            details.put(error.getField(), messageOf(error));
        }
        return build(HttpStatus.BAD_REQUEST, "Validation failed", request.getRequestURI(), details);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<RestApiError> handleConstraint(ConstraintViolationException exception, HttpServletRequest request) {
        Map<String, Object> details = new HashMap<>();
        exception.getConstraintViolations().forEach(v -> details.put(v.getPropertyPath().toString(), v.getMessage()));
        return build(HttpStatus.BAD_REQUEST, "Constraint violation", request.getRequestURI(), details);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<RestApiError> handleOther(Exception exception, HttpServletRequest request) {
        LOGGER.atWarn().setCause(exception).log("REST API error: {} - {}", HttpStatus.INTERNAL_SERVER_ERROR, request.getRequestURI());
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error", request.getRequestURI(), getDetails(exception));
    }

    private static ResponseEntity<RestApiError> build(HttpStatus status, String message, String path,
                                                      Map<String, Object> details) {
        var body = new RestApiError(status.value(), path);
        body.setMessage(message).setDetails(details);
        return ResponseEntity.status(status).body(body);
    }

    private Map<String, Object> getDetails(Throwable throwable) {
        Map<String, Object> details = new HashMap<>();
        details.put("type", throwable.getClass().getSimpleName());
        details.put("message", getRootCauseDescription(throwable));
        return details;
    }

    private static String messageOf(FieldError fe) {
        return fe.getDefaultMessage() != null ? fe.getDefaultMessage() : ("Invalid value for " + fe.getField());
    }
}
