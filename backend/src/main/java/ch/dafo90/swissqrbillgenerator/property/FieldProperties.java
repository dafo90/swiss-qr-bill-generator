package ch.dafo90.swissqrbillgenerator.property;

import ch.dafo90.swissqrbillgenerator.model.FieldType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
public record FieldProperties(
        @NotBlank String name,
        @NotBlank String defaultMap,
        @NotBlank String label,
        @NotNull Boolean required,
        String requiredText,
        @NotNull FieldType type,
        String defaultValue,
        List<String> options,
        @NotNull Boolean allowStaticValue) {
}
