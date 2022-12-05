package ch.dafo90.swissqrbillgenerator.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FieldGroup {

    private String name;
    private String label;
    private List<Field> fields;

}
