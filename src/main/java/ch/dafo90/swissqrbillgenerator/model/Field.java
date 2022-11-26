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
public class Field {

    private String name;
    private String defaultMap;
    private String label;
    private boolean required;
    private String requiredText;
    private FieldType type;
    private String defaultValue;
    private List<String> options;

}
