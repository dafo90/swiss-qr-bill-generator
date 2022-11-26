package ch.dafo90.swissqrbillgenerator.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BillData {

    @NotEmpty
    private List<Map<String, String>> csv;

    @NotEmpty
    private Map<String, FieldMap> fieldsMap;

    private String logoBase64;
}
