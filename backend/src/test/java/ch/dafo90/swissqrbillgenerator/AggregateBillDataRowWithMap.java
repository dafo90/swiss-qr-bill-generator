package ch.dafo90.swissqrbillgenerator;

import ch.dafo90.swissqrbillgenerator.mapper.BillDocumentMapper;
import ch.dafo90.swissqrbillgenerator.model.csv.Csv;
import ch.dafo90.swissqrbillgenerator.model.csv.Header;
import ch.dafo90.swissqrbillgenerator.model.csv.HeaderMap;
import ch.dafo90.swissqrbillgenerator.model.csv.Row;
import ch.dafo90.swissqrbillgenerator.util.CsvReader;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.aggregator.ArgumentsAggregationException;
import org.junit.jupiter.params.aggregator.ArgumentsAggregator;
import org.mozilla.universalchardet.UniversalDetector;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AggregateBillDataRowWithMap implements ArgumentsAggregator {

    private static Csv CSV;
    private static Map<String, String> FIELD_NAMES_MAP = new HashMap<>();

    static {
        // Document
        FIELD_NAMES_MAP.put(BillDocumentMapper.SALUTATION, "salutation_map");
        FIELD_NAMES_MAP.put(BillDocumentMapper.TEXT, "text_map");
        FIELD_NAMES_MAP.put(BillDocumentMapper.SIGNATURE_TITLE_1, "signature_title_1_map");
        FIELD_NAMES_MAP.put(BillDocumentMapper.SIGNATURE_NAME_1, "signature_name_1_map");
        FIELD_NAMES_MAP.put(BillDocumentMapper.SIGNATURE_TITLE_2, "signature_title_2_map");
        FIELD_NAMES_MAP.put(BillDocumentMapper.SIGNATURE_NAME_2, "signature_name_2_map");
        FIELD_NAMES_MAP.put(BillDocumentMapper.CLOSURE, "closure_map");
        // Bill
        FIELD_NAMES_MAP.put(BillDocumentMapper.ACCOUNT, "account_map");
        FIELD_NAMES_MAP.put(BillDocumentMapper.AMOUNT, "amount_map");
        FIELD_NAMES_MAP.put(BillDocumentMapper.CURRENCY, "currency_map");
        FIELD_NAMES_MAP.put(BillDocumentMapper.LANGUAGE, "language_map");
        FIELD_NAMES_MAP.put(BillDocumentMapper.REFERENCE, "reference_map");
        FIELD_NAMES_MAP.put(BillDocumentMapper.UNSTRUCTURED_MESSAGE, "unstructured_message_map");
        // Creditor
        FIELD_NAMES_MAP.put(BillDocumentMapper.CREDITOR_NAME, "creditor_name_map");
        FIELD_NAMES_MAP.put(BillDocumentMapper.CREDITOR_STREET, "creditor_street_map");
        FIELD_NAMES_MAP.put(BillDocumentMapper.CREDITOR_LOCALITY, "creditor_locality_map");
        FIELD_NAMES_MAP.put(BillDocumentMapper.CREDITOR_COUNTRY, "creditor_country_map");
        FIELD_NAMES_MAP.put(BillDocumentMapper.CREDITOR_EMAIL, "creditor_email_map");
        FIELD_NAMES_MAP.put(BillDocumentMapper.CREDITOR_WEBSITE, "creditor_website_map");
        FIELD_NAMES_MAP.put(BillDocumentMapper.CREDITOR_PHONE_NUMBER, "creditor_phone_number_map");
        // Debtor
        FIELD_NAMES_MAP.put(BillDocumentMapper.DEBTOR_TITLE, "debtor_title_map");
        FIELD_NAMES_MAP.put(BillDocumentMapper.DEBTOR_ORGANISATION, "debtor_organisation_map");
        FIELD_NAMES_MAP.put(BillDocumentMapper.DEBTOR_NAME, "debtor_name_map");
        FIELD_NAMES_MAP.put(BillDocumentMapper.DEBTOR_STREET, "debtor_street_map");
        FIELD_NAMES_MAP.put(BillDocumentMapper.DEBTOR_LOCALITY, "debtor_locality_map");
        FIELD_NAMES_MAP.put(BillDocumentMapper.DEBTOR_COUNTRY, "debtor_country_map");
    }

    @Override
    public Row aggregateArguments(ArgumentsAccessor argumentsAccessor, ParameterContext parameterContext) throws ArgumentsAggregationException {
        List<String> cells = new ArrayList<>();
        for (int i = 0; i < argumentsAccessor.size(); i++) {
            cells.add(argumentsAccessor.getString(i));
        }
        return new Row(cells);
    }

    public static List<HeaderMap> getHeadersMap() {
        return FIELD_NAMES_MAP.entrySet().stream().map(entry -> HeaderMap.builder()
                        .mapWithIndex(getIndexByColumn(entry.getValue()))
                        .fieldName(entry.getKey())
                        .build())
                .filter(headerMap -> headerMap.getMapWithIndex() >= 0)
                .toList();
    }

    private static Integer getIndexByColumn(String column) {
        Csv csv = getCsv();
        for (int i = 0; i < csv.getHeaders().size(); i++) {
            Header header = csv.getHeaders().get(i);
            if (header.getLabel().equals(column)) {
                return header.getColumnIndex();
            }
        }
        return -1;
    }

    private static Csv getCsv() {
        if (CSV == null) {
            try {
                File csvFile = ResourceUtils.getFile("classpath:data.csv");
                Charset charset = Charset.forName(UniversalDetector.detectCharset(csvFile));
                FileInputStream fileInputStream = new FileInputStream(csvFile);
                CSV = CsvReader.read(fileInputStream, ',', charset);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
        return CSV;
    }

    public static int getIndexOfField(List<HeaderMap> headersMap, String fieldName) {
        return headersMap.stream()
                .filter(headerMap -> headerMap.getFieldName().equals(fieldName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(String.format("Field '%s' not found", fieldName))).getMapWithIndex();
    }

}
