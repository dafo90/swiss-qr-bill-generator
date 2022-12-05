package ch.dafo90.swissqrbillgenerator.model.csv;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

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
