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

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidCredentials(InvalidCredentialsException ex,
                                                                     HttpServletRequest request) {
        return buildError(HttpStatus.UNAUTHORIZED, "AUTH-401", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(InactiveUserException.class)
    public ResponseEntity<ErrorResponseDto> handleInactiveUser(InactiveUserException ex,
                                                               HttpServletRequest request) {
        return buildError(HttpStatus.UNAUTHORIZED, "AUTH-401", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDto> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        return buildError(HttpStatus.BAD_REQUEST, "USR-400", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidationErrors(MethodArgumentNotValidException ex,
                                                                      HttpServletRequest request) {
        String fieldErrors = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(fe -> fe.getField() + ": " + (fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "Invalid value"))
                .collect(Collectors.joining(", "));

        return buildError(HttpStatus.BAD_REQUEST, "USR-400",
                "Validation failed - " + fieldErrors, request.getRequestURI());

    }

    private ResponseEntity<ErrorResponseDto> buildError(HttpStatus status, String code, String message, String path) {
        return ResponseEntity.status(status)
                .body(new ErrorResponseDto(code, message, Instant.now().toString(), path));
    }
}
