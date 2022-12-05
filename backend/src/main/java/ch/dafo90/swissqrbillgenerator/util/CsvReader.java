package ch.dafo90.swissqrbillgenerator.util;

import ch.dafo90.swissqrbillgenerator.model.csv.Csv;
import ch.dafo90.swissqrbillgenerator.model.csv.Header;
import ch.dafo90.swissqrbillgenerator.model.csv.Row;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class CsvReader {

    public static Csv read(InputStream is, char separator, Charset charset) {
        CSVParser parser = new CSVParserBuilder()
                .withSeparator(separator)
                .build();

        CSVReader csvReader = new CSVReaderBuilder(new InputStreamReader(is, charset))
                .withSkipLines(0)
                .withCSVParser(parser)
                .build();

        try {
            return mapCsv(csvReader.readAll());
        } catch (IOException | CsvException ex) {
            throw new RuntimeException("Cannot read CSV", ex);
        }
    }

    protected static Csv mapCsv(List<String[]> csv) {
        List<String[]> data = csv;
        String[] headers;

        // Remove empty rows before the header
        do {
            // Assume first row is the header, so we need at least 2 rows
            if (CollectionUtils.isEmpty(data) || data.size() == 1) {
                throw new RuntimeException("At least 2 rows are needed: one for the header and one or more for the data!");
            }
            headers = data.get(0);
            data = data.subList(1, data.size());
        } while (headers == null || headers.length == 0);

        // Remove empty rows
        data = data.stream().filter(row -> row != null && row.length > 0).toList();
        if (data.isEmpty()) {
            throw new RuntimeException("No data found in this CSV!");
        }

        List<Header> headersObj = new ArrayList<>();
        for (int i = 0; i < headers.length; i++) {
            headersObj.add(new Header(headers[i], i));
        }
        List<Row> rows = data.stream().map(dataRow -> mapToRow(dataRow, headersObj.size())).toList();
        return new Csv(headersObj, rows);
    }

    protected static Row mapToRow(String[] row, int size) {
        List<String> cells = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            cells.add(getValue(row, i));
        }
        return new Row(cells);
    }

    protected static String getValue(String[] row, int i) {
        return i < row.length ? row[i] : null;
    }

}
