package com.example.demo.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    public ProblemDetail problem(HttpStatus status, String title, String detail, HttpServletRequest request){
        ProblemDetail pd=ProblemDetail.forStatusAndDetail(status, detail);
        pd.setTitle(title);
        pd.setProperty("timestamp", OffsetDateTime.now());
        pd.setProperty("path", request.getRequestURI());
        return  pd;
    }

    @ExceptionHandler(CacheOperationException.class)
    public ResponseEntity<ProblemDetail> handleCacheOperation(CacheOperationException exception,HttpServletRequest request){
        log.warn("Cache operation exception",exception.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(problem(HttpStatus.SERVICE_UNAVAILABLE,"Cache service temporarily unavailable:",exception.getMessage(),request));
    }
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFound(ResourceNotFoundException exception, HttpServletRequest request){
        log.warn("Resource not found: {}", exception.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(problem(HttpStatus.NOT_FOUND,"Resource not found",exception.getMessage(),request));
    }
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ProblemDetail> handleBadRequest(BadRequestException exception,HttpServletRequest request){
        log.warn("bad request: {}", exception.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(problem(HttpStatus.BAD_REQUEST,"bad request",exception.getMessage(),request));
    }
    @ExceptionHandler(IntegrationException.class)
    public ResponseEntity<ProblemDetail> handleIntegration(IntegrationException exception, HttpServletRequest request){
        log.warn("Integration Error : {}", exception.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(problem(HttpStatus.SERVICE_UNAVAILABLE,"Upstream server error", exception.getMessage(),request));

    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest req) {
        ProblemDetail pd = problem(HttpStatus.BAD_REQUEST, "Constraint violation", ex.getMessage(), req);
        return ResponseEntity.badRequest().body(pd);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleUnreadable(HttpMessageNotReadableException ex, HttpServletRequest req) {
        ProblemDetail pd = problem(HttpStatus.BAD_REQUEST, "Malformed JSON", "Request body is invalid or unreadable.", req);
        return ResponseEntity.badRequest().body(pd);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetail> handleDbConflict(DataIntegrityViolationException ex, HttpServletRequest req) {
        log.warn("Data integrity violation", ex);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(problem(HttpStatus.CONFLICT, "Data integrity violation", "Request conflicts with existing data.", req));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleOther(Exception ex, HttpServletRequest req) {
        log.error("Unhandled error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(problem(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", "Something went wrong.", req));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException exception, HttpServletRequest request){
        Map<String, String> errors=new HashMap<>();
        for (FieldError error : exception.getBindingResult().getFieldErrors()) {
                errors.put(error.getField(),error.getDefaultMessage());
        }
        for (var error: exception.getBindingResult().getGlobalErrors()) {
            errors.put(error.getObjectName(),error.getDefaultMessage());
        }
        ProblemDetail pd=problem(HttpStatus.BAD_REQUEST,"Validation failed", "One or more field validations failed", request);
        pd.setProperty("error",errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(pd);
    }

}
