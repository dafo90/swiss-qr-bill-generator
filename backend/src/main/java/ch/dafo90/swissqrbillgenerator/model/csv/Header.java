package ch.dafo90.swissqrbillgenerator.model.csv;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Header {

    private String label;
    private int columnIndex;
}
