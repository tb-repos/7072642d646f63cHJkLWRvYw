package com.tourbhook.api.repository.exception;

import com.tourbhook.api.dto.common.ApiResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.tourbhook.api.service.MessageService;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MessageService messageService;

    @ExceptionHandler({ResourceNotFoundException.class, EntityNotFoundException.class})
    public ResponseEntity<ApiResponse<Void>> handleNotFound(Exception ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), null, request.getRequestURI());
    }

    @ExceptionHandler({BadRequestException.class, IllegalArgumentException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(Exception ex, HttpServletRequest request) {
        String message = ex instanceof HttpMessageNotReadableException
                ? messageService.get("error.malformed-request-body")
                : ex.getMessage();
        return build(HttpStatus.BAD_REQUEST, message, null, request.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return build(HttpStatus.BAD_REQUEST, messageService.get("error.validation-failed"), errors, request.getRequestURI());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, messageService.get("error.data-conflict"), ex.getMostSpecificCause().getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicate(DuplicateResourceException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), null, request.getRequestURI());
    }

    @ExceptionHandler(ContentModerationException.class)
    public ResponseEntity<ApiResponse<Void>> handleContentModeration(ContentModerationException ex, HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage(), null, request.getRequestURI());
    }

    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<ApiResponse<Void>> handleExternalService(ExternalServiceException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_GATEWAY, ex.getMessage(), null, request.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneric(Exception ex, HttpServletRequest request) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, messageService.get("error.unexpected"), ex.getMessage(), request.getRequestURI());
    }

    private ResponseEntity<ApiResponse<Void>> build(HttpStatus status, String message, Object errors, String path) {
        return ResponseEntity.status(status).body(
                ApiResponse.<Void>builder()
                        .success(false)
                        .message(message)
                        .errors(errors)
                        .timestamp(LocalDateTime.now())
                        .path(path)
                        .build()
        );
    }
}
