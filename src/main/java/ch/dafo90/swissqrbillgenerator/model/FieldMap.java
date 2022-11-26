package ch.dafo90.swissqrbillgenerator.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FieldMap {

    @NotBlank
    private String mapWith;

    private String staticValue;

}
