package ch.dafo90.swissqrbillgenerator.property;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@ConstructorBinding
@ConfigurationProperties("app")
@Validated
public record AppProperties(@NotEmpty List<FieldGroupProperties> groups) {
}
