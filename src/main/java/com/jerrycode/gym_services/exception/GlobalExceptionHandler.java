package com.jerrycode.gym_services.exception;

import com.jerrycode.gym_services.response.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleResourceNotFound(ResourceNotFoundException ex, WebRequest request) {
        if (isPdfRequest(request)) {
            return null; // Let the controller handle it
        }
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGenericException(Exception ex, WebRequest request) {
        if (isPdfRequest(request)) {
            return null; // Let the controller handle it
        }
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }


    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Response<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Response<Void>> handleValidationErrors(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Validation failed: " + message);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Response<Void>> handleAccessDenied(AccessDeniedException ex) {
        return buildErrorResponse(HttpStatus.FORBIDDEN, "Access denied: Insufficient permissions");
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Response<Void>> handleSecurityException(SecurityException ex) {
        return buildErrorResponse(HttpStatus.FORBIDDEN, "Insufficient permissions: " + ex.getMessage());
    }

    private boolean isPdfRequest(WebRequest request) {
        String path = request.getDescription(false);
        return path != null && path.contains("/invoice-report/");
    }

    private ResponseEntity<Response<Void>> buildErrorResponse(HttpStatus status, String message) {
        Response<Void> response = Response.<Void>builder()
                .success(false)
                .message(message)
                .build();
        return ResponseEntity.status(status).body(response);
    }
}