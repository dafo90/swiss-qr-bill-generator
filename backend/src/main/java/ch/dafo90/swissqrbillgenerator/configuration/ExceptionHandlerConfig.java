package ch.dafo90.swissqrbillgenerator.configuration;

import ch.dafo90.swissqrbillgenerator.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class ExceptionHandlerConfig {

    @Bean
    public ErrorAttributes errorAttributes() {
        return new DefaultErrorAttributes() {
            @Override
            public Map<String, Object> getErrorAttributes(WebRequest webRequest, ErrorAttributeOptions options) {
                Map<String, Object> errorAttributes = super.getErrorAttributes(webRequest, options);
                Throwable error = getError(webRequest);
                if (error instanceof ValidationException validationException) {
                    errorAttributes.put("validationMessages", validationException.getMessages());
                }
                return errorAttributes;
            }

        };
    }
}