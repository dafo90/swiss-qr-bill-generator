package ch.dafo90.swissqrbillgenerator.property;

import ch.dafo90.swissqrbillgenerator.model.FieldType;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@ConstructorBinding
@Validated
public record FieldProperties(@NotBlank String name, @NotBlank String defaultMap, @NotBlank String label,
                              @NotNull Boolean required, String requiredText, @NotNull FieldType type,
                              String defaultValue, List<String> options, @NotNull Boolean allowStaticValue) {
}
