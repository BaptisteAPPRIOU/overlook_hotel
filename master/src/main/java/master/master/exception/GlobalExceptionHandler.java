package master.master.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import master.master.web.rest.dto.ErrorResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.server.ResponseStatusException;

/**
 * Global exception handler for the application that provides centralized exception handling across
 * all controllers using Spring's @ControllerAdvice annotation.
 *
 * <p>This class intercepts exceptions thrown by controllers and converts them into appropriate HTTP
 * responses with structured error information.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ErrorResponseDto> handleAccessDeniedException(
      AccessDeniedException ex, HttpServletRequest request) {
    return build(HttpStatus.FORBIDDEN, "ACCESS_DENIED", "Access denied", request.getRequestURI());
  }

  @ExceptionHandler({AuthenticationException.class, BadCredentialsException.class})
  public ResponseEntity<ErrorResponseDto> handleAuthenticationException(
      Exception ex, HttpServletRequest request) {
    return build(
        HttpStatus.UNAUTHORIZED,
        "NOT_AUTHENTICATED",
        "Authentication required",
        request.getRequestURI());
  }

  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<ErrorResponseDto> handleResponseStatusException(
      ResponseStatusException ex, HttpServletRequest request) {
    HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
    return build(status, codeFor(status), ex.getReason(), request.getRequestURI());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponseDto> handleValidationException(
      MethodArgumentNotValidException ex, HttpServletRequest request) {
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult()
        .getFieldErrors()
        .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

    ErrorResponseDto body =
        ErrorResponseDto.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .code("VALIDATION_ERROR")
            .message("Validation failed")
            .timestamp(java.time.LocalDateTime.now())
            .path(request.getRequestURI())
            .errors(errors)
            .build();

    return ResponseEntity.badRequest().body(body);
  }

  @ExceptionHandler({
    HttpMessageNotReadableException.class,
    MissingServletRequestParameterException.class,
    IllegalArgumentException.class
  })
  public ResponseEntity<ErrorResponseDto> handleBadRequest(
      Exception ex, HttpServletRequest request) {
    return build(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "Invalid request", request.getRequestURI());
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponseDto> handleGeneralException(
      Exception ex, HttpServletRequest request) {
    return build(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "INTERNAL_ERROR",
        "An unexpected error occurred",
        request.getRequestURI());
  }

  private ResponseEntity<ErrorResponseDto> build(
      HttpStatus status, String code, String message, String path) {
    return ResponseEntity.status(status)
        .body(
            ErrorResponseDto.builder()
                .status(status.value())
                .code(code)
                .message(message != null && !message.isBlank() ? message : status.getReasonPhrase())
                .timestamp(java.time.LocalDateTime.now())
                .path(path)
                .build());
  }

  private String codeFor(HttpStatus status) {
    return switch (status) {
      case BAD_REQUEST -> "BAD_REQUEST";
      case UNAUTHORIZED -> "NOT_AUTHENTICATED";
      case FORBIDDEN -> "ACCESS_DENIED";
      case NOT_FOUND -> "NOT_FOUND";
      case CONFLICT -> "CONFLICT";
      default -> status.is5xxServerError() ? "INTERNAL_ERROR" : "HTTP_ERROR";
    };
  }
}
