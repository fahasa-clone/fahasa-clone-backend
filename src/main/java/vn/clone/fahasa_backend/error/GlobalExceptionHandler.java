package vn.clone.fahasa_backend.error;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<String> handleBadRequestException(BadRequestException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(InvalidAccountException.class)
    public ResponseEntity<String> handleUsernameNotFoundException(InvalidAccountException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
