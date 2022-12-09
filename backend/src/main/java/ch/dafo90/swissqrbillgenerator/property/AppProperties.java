package ch.dafo90.swissqrbillgenerator.property;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@ConfigurationProperties("app")
@Validated
public record AppProperties(@NotEmpty List<FieldGroupProperties> groups) {
}
