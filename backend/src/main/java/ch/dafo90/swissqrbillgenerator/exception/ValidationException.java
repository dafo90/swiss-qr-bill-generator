package ch.dafo90.swissqrbillgenerator.exception;

import ch.dafo90.swissqrbillgenerator.model.validation.ValidationMessage;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

@Data
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class ValidationException extends RuntimeException {

    private final List<ValidationMessage> messages;

    public ValidationException(List<ValidationMessage> messages) {
        this.messages = messages;
    }

    public ValidationException(String message, List<ValidationMessage> messages) {
        super(message);
        this.messages = messages;
    }

    public ValidationException(String message, Throwable cause, List<ValidationMessage> messages) {
        super(message, cause);
        this.messages = messages;
    }

    public ValidationException(Throwable cause, List<ValidationMessage> messages) {
        super(cause);
        this.messages = messages;
    }

    public ValidationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, List<ValidationMessage> messages) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.messages = messages;
    }
}
