package ch.dafo90.swissqrbillgenerator.exception;

import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Data
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class ImageException extends RuntimeException {

    public static final String INVALID_IMAGE = "invalid_image";
    public static final String UNSUPPORTED_IMAGE_MEDIA_TYPE = "unsupported_image_media_type";

    private final String messageKey;

    public ImageException(String messageKey) {
        this.messageKey = messageKey;
    }

    public ImageException(String message, String messageKey) {
        super(message);
        this.messageKey = messageKey;
    }

    public ImageException(String message, Throwable cause, String messageKey) {
        super(message, cause);
        this.messageKey = messageKey;
    }

    public ImageException(Throwable cause, String messageKey) {
        super(cause);
        this.messageKey = messageKey;
    }

    public ImageException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, String messageKey) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.messageKey = messageKey;
    }
}
