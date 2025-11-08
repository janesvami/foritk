package ani.foritk.exception;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Set;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class APIExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<APIError> handleEntityNotFound(EntityNotFoundException ex) {
        log.error(ex.getMessage(), ex);
        final HttpStatus status = HttpStatus.NOT_FOUND;
        final APIError apiError = new APIError(status, ex.getMessage());
        return constructApiErrorWithHttpStatus(apiError);
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<APIError> handleInsufficientFunds(InsufficientFundsException ex) {
        log.error(ex.getMessage(), ex);
        final HttpStatus status = HttpStatus.BAD_REQUEST;
        final APIError apiError = new APIError(status, ex.getMessage());
        return constructApiErrorWithHttpStatus(apiError);

    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<APIError> handleBadJson(HttpMessageNotReadableException ex) {
        final APIError apiError = new APIError(
                HttpStatus.BAD_REQUEST,
                "Request validation failed: " + ex.getMessage()
        );
        return constructApiErrorWithHttpStatus(apiError);
    }

    @ExceptionHandler(value = {MethodArgumentNotValidException.class})
    public ResponseEntity<APIError> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        final Set<String> errs = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> {
                    String field = fieldError.getField();
                    String message = fieldError.getDefaultMessage();
                    return "Field '" + field + "' is invalid: " + message;
                })
                .collect(Collectors.toSet());

        final APIError apiError = new APIError(
                HttpStatus.BAD_REQUEST,
                "Request validation failed: " + errs
        );
        return constructApiErrorWithHttpStatus(apiError);
    }

    private ResponseEntity<APIError> constructApiErrorWithHttpStatus(APIError apiError) {
        final var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new ResponseEntity<>(apiError, headers, apiError.status());
    }
}