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
import org.postgresql.util.PSQLException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
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
 * <p>
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
     * Handle data integrity violation exceptions.
     * <p>
     * Catches database exceptions for all constraint violations:
     * - Foreign key constraints
     * - Unique constraints
     * - NOT NULL constraints
     * - Check constraints
     * - Column value out of range
     * - Referential integrity issues
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetail> handleDataIntegrityViolation(DataIntegrityViolationException ex,
                                                                      WebRequest request) {

        log.warn("Data integrity violation: {}", ex.getMessage());

        // Extract information from the exception
        String detail = ex.getMessage();
        String title = "Data Integrity Violation";
        String type = "https://example.com/probs/data-integrity-violation";
        HttpStatus statusCode = HttpStatus.CONFLICT;

        // Traverse the entire cause chain to find PSQLException
        Throwable cause = ex.getCause();
        while (cause != null) {
            if (cause instanceof PSQLException psqlEx) {
                String psqlMessage = psqlEx.getMessage();

                if (psqlMessage != null) {
                    // Foreign key constraint violation
                    if (psqlMessage.contains("foreign key constraint") ||
                        psqlMessage.contains("violates foreign key constraint")) {
                        title = "Entity Referenced";
                        type = "https://example.com/probs/entity-referenced";
                        detail = "Cannot perform this operation because this record is still referenced by other records in the database.";
                        log.info("Detected foreign key constraint violation");
                        break;
                    }

                    // Unique constraint violation
                    if (psqlMessage.contains("unique constraint") ||
                        psqlMessage.contains("violates unique constraint") ||
                        psqlMessage.contains("Duplicate entry")) {
                        title = "Duplicate Record";
                        type = "https://example.com/probs/duplicate-entry";
                        detail = extractUniqueConstraintMessage(psqlMessage,
                                                                "A record with this value already exists. Please use a different value.");
                        log.info("Detected unique constraint violation");
                        break;
                    }

                    // NOT NULL constraint violation
                    if (psqlMessage.contains("not-null constraint") ||
                        psqlMessage.contains("violates not-null constraint") ||
                        psqlMessage.contains("Column cannot be null") ||
                        psqlMessage.contains("NULL in column")) {
                        title = "Missing Required Field";
                        type = "https://example.com/probs/missing-required-field";
                        detail = extractNotNullConstraintMessage(psqlMessage,
                                                                 "A required field is missing or null. Please provide all mandatory fields.");
                        log.info("Detected NOT NULL constraint violation");
                        break;
                    }

                    // Check constraint violation
                    if (psqlMessage.contains("check constraint") ||
                        psqlMessage.contains("violates check constraint")) {
                        title = "Invalid Field Value";
                        type = "https://example.com/probs/invalid-field-value";
                        detail = extractCheckConstraintMessage(psqlMessage,
                                                               "The provided value does not meet the required criteria.");
                        log.info("Detected check constraint violation");
                        break;
                    }

                    // Value out of range
                    if (psqlMessage.contains("out of range") ||
                        psqlMessage.contains("numeric value out of range")) {
                        title = "Value Out of Range";
                        type = "https://example.com/probs/value-out-of-range";
                        detail = "The provided value is out of range for this field. Please provide a valid value.";
                        log.info("Detected value out of range violation");
                        break;
                    }

                    // Data type mismatch or other integrity issues
                    if (psqlMessage.contains("data type") ||
                        psqlMessage.contains("type mismatch")) {
                        title = "Invalid Data Type";
                        type = "https://example.com/probs/invalid-data-type";
                        detail = "The provided data type is invalid for this field. Please check your input.";
                        log.info("Detected data type mismatch violation");
                        break;
                    }
                }
            }

            // Move to the next cause in the chain
            cause = cause.getCause();
        }

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(statusCode, detail);
        problemDetail.setType(URI.create(type));
        problemDetail.setTitle(title);
        problemDetail.setProperty("timestamp", Instant.now());

        return ResponseEntity.status(statusCode)
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

    /**
     * Extract meaningful message from unique constraint violation.
     */
    private String extractUniqueConstraintMessage(String psqlMessage, String defaultMessage) {
        // Try to extract the constraint name or field name
        if (psqlMessage.contains("\"")) {
            String[] parts = psqlMessage.split("\"");
            if (parts.length >= 2) {
                return String.format("The value for '%s' already exists. Please use a different value.", parts[1]);
            }
        }
        return defaultMessage;
    }

    /**
     * Extract meaningful message from NOT NULL constraint violation.
     */
    private String extractNotNullConstraintMessage(String psqlMessage, String defaultMessage) {
        // Try to extract the column name
        if (psqlMessage.contains("\"")) {
            String[] parts = psqlMessage.split("\"");
            if (parts.length >= 2) {
                return String.format("The field '%s' is required and cannot be empty.", parts[1]);
            }
        }
        return defaultMessage;
    }

    /**
     * Extract meaningful message from check constraint violation.
     */
    private String extractCheckConstraintMessage(String psqlMessage, String defaultMessage) {
        // Try to extract the constraint name
        if (psqlMessage.contains("\"")) {
            String[] parts = psqlMessage.split("\"");
            if (parts.length >= 2) {
                return String.format("The provided value violates the constraint '%s'.", parts[1]);
            }
        }
        return defaultMessage;
    }
}
