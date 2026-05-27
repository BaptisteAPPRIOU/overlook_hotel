package master.master.exception;

import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

/**
 * Global exception handler for the application that provides centralized exception handling across
 * all controllers using Spring's @ControllerAdvice annotation.
 *
 * <p>This class intercepts exceptions thrown by controllers and converts them into appropriate HTTP
 * responses with structured error information.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<Map<String, Object>> handleResponseStatusException(
      ResponseStatusException ex) {
    Map<String, Object> body = new HashMap<>();
    body.put("message", ex.getReason());
    return new ResponseEntity<>(body, ex.getStatusCode());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> handleValidationException(
      MethodArgumentNotValidException ex) {
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult()
        .getFieldErrors()
        .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

    Map<String, Object> body = new HashMap<>();
    body.put("status", HttpStatus.BAD_REQUEST.value());
    body.put("message", "Validation failed");
    body.put("errors", errors);

    return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleGeneralException(Exception ex) {
    Map<String, Object> body = new HashMap<>();
    body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
    body.put("message", "An unexpected error occurred");

    body.put("trace", ex.toString());

    return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
