package ch.dafo90.swissqrbillgenerator.model.csv;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Csv {

    List<Header> headers;
    List<Row> rows;

}
