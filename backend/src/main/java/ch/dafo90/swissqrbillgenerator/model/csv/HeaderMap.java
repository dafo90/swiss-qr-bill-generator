package ch.dafo90.swissqrbillgenerator.model.csv;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HeaderMap {

    @NotBlank
    private String fieldName;

    @NotNull
    private Integer mapWithIndex;

    private String staticValue;


}
