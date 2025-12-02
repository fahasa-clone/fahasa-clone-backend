package vn.clone.fahasa_backend.error;

import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Global exception handler for the application.
 * Centralized exception handling across the entire application using @RestControllerAdvice.
 */
@Slf4j
@RestControllerAdvice
// public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
public class GlobalExceptionHandler {

    /**
     * Handle method argument validation exceptions.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                      WebRequest request) {

        log.warn("Validation error: {}", ex.getMessage());

        BindingResult bindingResult = ex.getBindingResult();
        List<String> fieldErrors = new ArrayList<>(bindingResult.getFieldErrors()
                                                                .stream()
                                                                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                                                                .toList());

        List<String> globalErrors = bindingResult.getGlobalErrors()
                                                 .stream()
                                                 .map(DefaultMessageSourceResolvable::getDefaultMessage)
                                                 .toList();

        fieldErrors.addAll(globalErrors);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                                                                       "Validation failed");
        problemDetail.setType(URI.create("https://example.com/probs/invalid-argument"));
        problemDetail.setTitle("Invalid Argument");
        problemDetail.setProperty("errors", fieldErrors);
        problemDetail.setProperty("timestamp", Instant.now());

        return ResponseEntity.badRequest()
                             .body(problemDetail);
    }

    /**
     * Handle constraint violation exceptions.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleConstraintViolation(ConstraintViolationException ex,
                                                                   WebRequest request) {

        log.warn("Constraint violation: {}", ex.getMessage());

        Map<String, String> violations = new HashMap<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            violations.put(violation.getPropertyPath()
                                    .toString(), violation.getMessage());
        }

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                                                                       "Constraint violation");
        problemDetail.setType(URI.create("https://example.com/probs/constraint-violation"));
        problemDetail.setTitle("Constraint Violation");
        problemDetail.setProperty("violations", violations);
        problemDetail.setProperty("timestamp", Instant.now());

        return ResponseEntity.badRequest()
                             .body(problemDetail);
    }

    /**
     * Handle entity not found exceptions.
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleEntityNotFound(EntityNotFoundException ex,
                                                              WebRequest request) {

        log.warn("Entity not found: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,
                                                                       ex.getMessage());
        problemDetail.setType(URI.create("https://example.com/probs/entity-not-found"));
        problemDetail.setTitle("Entity Not Found");
        problemDetail.setProperty("timestamp", Instant.now());

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                             .body(problemDetail);
    }

    /**
     * Handle custom bad request exceptions.
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ProblemDetail> handleBadRequestException(BadRequestException ex,
                                                                   WebRequest request) {

        log.warn("Bad request: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                                                                       ex.getMessage());
        problemDetail.setType(URI.create("https://example.com/probs/bad-request"));
        problemDetail.setTitle("Bad Request");
        problemDetail.setProperty("timestamp", Instant.now());

        return ResponseEntity.badRequest()
                             .body(problemDetail);
    }

    /**
     * Handle access denied exceptions.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleAccessDenied(AccessDeniedException ex,
                                                            WebRequest request) {

        log.warn("Access denied: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN,
                                                                       "Access denied");
        problemDetail.setType(URI.create("https://example.com/probs/access-denied"));
        problemDetail.setTitle("Access Denied");
        problemDetail.setProperty("timestamp", Instant.now());

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                             .body(problemDetail);
    }

    /**
     * Handle bad credentials exceptions.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ProblemDetail> handleBadCredentials(BadCredentialsException ex,
                                                              WebRequest request) {

        log.warn("Bad credentials");

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED,
                                                                       "Invalid credentials");
        problemDetail.setType(URI.create("https://example.com/probs/bad-credentials"));
        problemDetail.setTitle("Bad Credentials");
        problemDetail.setProperty("timestamp", Instant.now());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                             .body(problemDetail);
    }

    /**
     * Handle method argument type mismatch exceptions.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ProblemDetail> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                                          WebRequest request) {

        log.warn("Method argument type mismatch: {}", ex.getMessage());

        String errorMessage = String.format("Parameter '%s' should be of type %s",
                                            ex.getName(),
                                            ex.getRequiredType()
                                              .getSimpleName());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                                                                       errorMessage);
        problemDetail.setType(URI.create("https://example.com/probs/type-mismatch"));
        problemDetail.setTitle("Type Mismatch");
        problemDetail.setProperty("timestamp", Instant.now());

        return ResponseEntity.badRequest()
                             .body(problemDetail);
    }

    /**
     * Handle generic exceptions.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGlobalException(Exception ex,
                                                               WebRequest request) {

        log.error("An unexpected error occurred", ex);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please try again later."
        );
        problemDetail.setType(URI.create("https://example.com/probs/internal-error"));
        problemDetail.setTitle("Internal Server Error");
        problemDetail.setProperty("timestamp", Instant.now());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                             .body(problemDetail);
    }
}
