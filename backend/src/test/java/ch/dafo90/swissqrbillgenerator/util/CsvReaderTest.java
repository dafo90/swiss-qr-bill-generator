package ch.dafo90.swissqrbillgenerator.util;

import ch.dafo90.swissqrbillgenerator.model.csv.Csv;
import ch.dafo90.swissqrbillgenerator.model.csv.Row;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mozilla.universalchardet.UniversalDetector;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class CsvReaderTest {

    @Test
    void read() throws IOException {
        File csvFile = ResourceUtils.getFile("classpath:data.csv");
        assertTrue(csvFile.exists());
        assertTrue(csvFile.isFile());

        Charset charset = Charset.forName(UniversalDetector.detectCharset(csvFile));
        assertNotNull(charset);

        FileInputStream fileInputStream = new FileInputStream(csvFile);
        Csv csv = CsvReader.read(fileInputStream, ',', charset);
        assertEquals(24, csv.getHeaders().size());
        assertEquals(1, csv.getRows().size());
        assertEquals(24, csv.getRows().get(0).getCells().size());
    }

    private static Stream<List<String[]>> getInvalidCsvRows() {
        String[] row = new String[]{"a", "b", "c"};

        List<String[]> oneRow = new ArrayList<>();
        oneRow.add(row);

        List<String[]> emptyRows = new ArrayList<>();
        emptyRows.add(null);
        emptyRows.add(new String[]{});

        return Stream.of(
                null,
                List.of(),
                oneRow,
                emptyRows
        );
    }

    @ParameterizedTest
    @MethodSource("getInvalidCsvRows")
    void mapCsv_noRows(List<String[]> csvRows) {
        RuntimeException ex = assertThrows(RuntimeException.class, () -> CsvReader.mapCsv(csvRows));
        assertEquals("At least 2 rows are needed: one for the header and one or more for the data!", ex.getMessage());
    }

    private static Stream<List<String[]>> getValidCsvRows() {
        String[] row = new String[]{"a", "b", "c"};

        List<String[]> rowsWithoutEmptyRows = new ArrayList<>();
        rowsWithoutEmptyRows.add(row);
        rowsWithoutEmptyRows.add(row);
        rowsWithoutEmptyRows.add(row);

        List<String[]> rowsWithEmptyRows = new ArrayList<>();
        rowsWithEmptyRows.add(null);
        rowsWithEmptyRows.add(new String[]{});
        rowsWithEmptyRows.add(row);
        rowsWithEmptyRows.add(null);
        rowsWithEmptyRows.add(new String[]{});
        rowsWithEmptyRows.add(row);
        rowsWithEmptyRows.add(null);
        rowsWithEmptyRows.add(new String[]{});
        rowsWithEmptyRows.add(row);
        rowsWithEmptyRows.add(null);
        rowsWithEmptyRows.add(new String[]{});

        return Stream.of(
                rowsWithoutEmptyRows,
                rowsWithEmptyRows
        );
    }

    @ParameterizedTest
    @MethodSource("getValidCsvRows")
    void mapCsv(List<String[]> csvRows) {
        Csv csv = CsvReader.mapCsv(csvRows);

        assertEquals(3, csv.getHeaders().size());
        assertEquals(2, csv.getRows().size());
    }

    @Test
    void mapToRow() {
        String[] row = new String[]{"a", "b", "c"};
        assertEquals(new Row(List.of("a", "b")), CsvReader.mapToRow(row, 2));
        assertEquals(new Row(List.of("a", "b", "c")), CsvReader.mapToRow(row, 3));
        assertEquals(new Row(Arrays.asList("a", "b", "c", null)), CsvReader.mapToRow(row, 4));
    }

    @Test
    void getValue() {
        String[] row = new String[]{"a", "b", "c"};
        assertEquals("a", CsvReader.getValue(row, 0));
        assertEquals("b", CsvReader.getValue(row, 1));
        assertEquals("c", CsvReader.getValue(row, 2));
        assertNull(CsvReader.getValue(row, 3));
    }
}
