package jalau.cis.api.exception;

import jakarta.servlet.http.HttpServletRequest;
import jalau.cis.api.dto.ErrorResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleUserNotFound(UserNotFoundException ex, HttpServletRequest request) {
        return buildError(HttpStatus.NOT_FOUND, "USR-404", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(DuplicateLoginException.class)
    public ResponseEntity<ErrorResponseDto> handleDuplicateLogin(DuplicateLoginException ex, HttpServletRequest request) {
        return buildError(HttpStatus.CONFLICT, "USR-409", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDto> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        return buildError(HttpStatus.BAD_REQUEST, "USR-400", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex,
                                                                      HttpServletRequest request) {
        List<Map<String, String>> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map((FieldError fe) -> Map.of(
                        "field", fe.getField(),
                        "message", fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "Invalid value"
                ))
                .collect(Collectors.toList());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "code", "USR-400",
                        "message", "Validation failed",
                        "timestamp", Instant.now().toString(),
                        "path", request.getRequestURI(),
                        "errors", fieldErrors
                ));
    }

    private ResponseEntity<ErrorResponseDto> buildError(HttpStatus status, String code, String message, String path) {
        return ResponseEntity.status(status)
                .body(new ErrorResponseDto(code, message, Instant.now().toString(), path));
    }
}
