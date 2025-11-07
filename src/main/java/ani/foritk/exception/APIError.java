package ani.foritk.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record APIError(
        HttpStatus status,
        String message,
        String hint
) {
    public APIError(HttpStatus status, String message){
        this(status, message, null);
    }
}