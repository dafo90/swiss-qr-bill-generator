package ch.dafo90.swissqrbillgenerator.property;

import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@ConstructorBinding
@Validated
public record FieldGroupProperties(@NotBlank String name, @NotBlank String label,
                                   @NotEmpty List<FieldProperties> fields) {
}
