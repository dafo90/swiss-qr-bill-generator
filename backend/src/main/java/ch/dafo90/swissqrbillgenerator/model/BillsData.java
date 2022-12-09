package ch.dafo90.swissqrbillgenerator.model;

import ch.dafo90.swissqrbillgenerator.model.csv.HeaderMap;
import ch.dafo90.swissqrbillgenerator.model.csv.Row;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BillsData {

    @NotEmpty
    private List<Row> data;

    @NotEmpty
    private List<HeaderMap> headersMap;

    private String logoBase64;
}
