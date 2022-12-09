package ch.dafo90.swissqrbillgenerator.property;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
public record FieldGroupProperties(
        @NotBlank String name,
        @NotBlank String label,
        @NotEmpty List<FieldProperties> fields) {
}
