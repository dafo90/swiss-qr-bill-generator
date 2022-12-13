package ch.dafo90.swissqrbillgenerator.model.validation;

import lombok.Data;

import java.util.List;

@Data
public class ValidationMessage {

    public static final String REQUIRED = "required";
    public static final String UNSUPPORTED_STRING = "unsupported_string";
    public static final String INVALID_URL = "invalid_url";
    public static final String INVALID_NUMBER = "invalid_number";
    public static final String UNSUPPORTED_NUMBER = "unsupported_number";
    public static final String INVALID_EMAIL = "invalid_email";
    public static final String INVALID_COUNTRY_CODE = "invalid_country_code";
    public static final String UNSUPPORTED_COUNTRY_CODE = "unsupported_country_code";
    public static final String UNSUPPORTED_CURRENCY_CODE = "unsupported_currency_code";
    public static final String UNSUPPORTED_LANGUAGE_CODE = "unsupported_language_code";
    public static final String INVALID_FIELD_NAME = "invalid_field_name";
    public static final String INVALID_MEDIA_TYPE = "invalid_media_type";

    private String field;
    private String rejectedValue;
    private String message;
    private String messageKey;
    private List<String> messageParameters;

    public ValidationMessage(String field, String rejectedValue, String message, String messageKey, String... messageParameters) {
        this.field = field;
        this.rejectedValue = rejectedValue;
        this.message = message;
        this.messageKey = messageKey;
        this.messageParameters = List.of(messageParameters);
    }
}
