package com.inguzdev.gitly.exception;

import com.inguzdev.gitly.dto.ApiError;
import com.inguzdev.gitly.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @Value("${app.debug:false}")
    private boolean debugMode;

    /**
     * Handle validation errors (from @Valid annotation)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        log.warn("Validation error on request: {}", request.getRequestURI(), ex);

        List<ApiError.ValidationError> validationErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::mapFieldError)
                .collect(Collectors.toList());

        ApiError error = ApiError.builder()
                .code("VALIDATION_ERROR")
                .details("Request validation failed")
                .validationErrors(validationErrors)
                .build();

        ApiResponse<Void> response = ApiResponse.<Void>error("Validation failed", error)
                .withPath(request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handle type mismatch errors (e.g., invalid path variables)
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {

        log.warn("Type mismatch error on request: {}", request.getRequestURI(), ex);

        String message = String.format("Invalid value '%s' for parameter '%s'. Expected type: %s",
                ex.getValue(),
                ex.getName(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");

        ApiError error = ApiError.builder()
                .code("TYPE_MISMATCH")
                .details(message)
                .build();

        ApiResponse<Void> response = ApiResponse.<Void>error("Invalid parameter type", error)
                .withPath(request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handle 404 - Not Found
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(
            NoHandlerFoundException ex,
            HttpServletRequest request) {

        log.warn("Resource not found: {}", request.getRequestURI());

        ApiError error = ApiError.builder()
                .code("RESOURCE_NOT_FOUND")
                .details(String.format("No handler found for %s %s", ex.getHttpMethod(), ex.getRequestURL()))
                .build();

        ApiResponse<Void> response = ApiResponse.<Void>error("Resource not found", error)
                .withPath(request.getRequestURI());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * Handle custom business exceptions
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(
            BusinessException ex,
            HttpServletRequest request) {

        log.warn("Business exception on request: {}", request.getRequestURI(), ex);

        ApiError error = ApiError.builder()
                .code(ex.getErrorCode())
                .details(ex.getMessage())
                .build();

        ApiResponse<Void> response = ApiResponse.<Void>failure(ex.getMessage(), null)
                .withPath(request.getRequestURI());
        response.setError(error);

        return ResponseEntity.status(ex.getHttpStatus()).body(response);
    }

    /**
     * Handle all other unexpected exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(
            Exception ex,
            HttpServletRequest request) {

        log.error("Unexpected error on request: {}", request.getRequestURI(), ex);

        ApiError.ApiErrorBuilder errorBuilder = ApiError.builder()
                .code("INTERNAL_SERVER_ERROR")
                .details("An unexpected error occurred. Please try again later.");

        // Include stack trace only in debug mode
        if (debugMode) {
            errorBuilder.stackTrace(getStackTrace(ex));
        }

        ApiError error = errorBuilder.build();

        ApiResponse<Void> response = ApiResponse.<Void>error("Internal server error", error)
                .withPath(request.getRequestURI());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * Map Spring's FieldError to our ValidationError
     */
    private ApiError.ValidationError mapFieldError(FieldError fieldError) {
        return ApiError.ValidationError.builder()
                .field(fieldError.getField())
                .rejectedValue(fieldError.getRejectedValue())
                .message(fieldError.getDefaultMessage())
                .build();
    }

    /**
     * Get stack trace as string
     */
    private String getStackTrace(Exception ex) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        return sw.toString();
    }
}
