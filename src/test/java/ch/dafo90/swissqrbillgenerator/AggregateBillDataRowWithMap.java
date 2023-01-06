package ch.dafo90.swissqrbillgenerator;

import ch.dafo90.swissqrbillgenerator.mapper.BillDocumentMapper;
import ch.dafo90.swissqrbillgenerator.model.FieldMap;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.aggregator.ArgumentsAggregationException;
import org.junit.jupiter.params.aggregator.ArgumentsAggregator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AggregateBillDataRowWithMap implements ArgumentsAggregator {

    public static final List<String> COLUMNS = List.of(
            // Document
            BillDocumentMapper.SALUTATION,
            BillDocumentMapper.TEXT,
            BillDocumentMapper.SIGNATURE_TITLE_1,
            BillDocumentMapper.SIGNATURE_NAME_1,
            BillDocumentMapper.SIGNATURE_TITLE_2,
            BillDocumentMapper.SIGNATURE_NAME_2,
            BillDocumentMapper.CLOSURE,
            // Bill
            BillDocumentMapper.ACCOUNT,
            BillDocumentMapper.AMOUNT,
            BillDocumentMapper.CURRENCY,
            BillDocumentMapper.LANGUAGE,
            BillDocumentMapper.REFERENCE,
            BillDocumentMapper.UNSTRUCTURED_MESSAGE,
            // Creditor
            BillDocumentMapper.CREDITOR_NAME,
            BillDocumentMapper.CREDITOR_STREET,
            BillDocumentMapper.CREDITOR_LOCALITY,
            BillDocumentMapper.CREDITOR_COUNTRY,
            BillDocumentMapper.CREDITOR_EMAIL,
            BillDocumentMapper.CREDITOR_WEBSITE,
            BillDocumentMapper.CREDITOR_PHONE_NUMBER,
            // Debtor
            BillDocumentMapper.DEBTOR_TITLE,
            BillDocumentMapper.DEBTOR_ORGANIZATION,
            BillDocumentMapper.DEBTOR_NAME,
            BillDocumentMapper.DEBTOR_STREET,
            BillDocumentMapper.DEBTOR_LOCALITY,
            BillDocumentMapper.DEBTOR_COUNTRY
    );

    @Override
    public Map<String, String> aggregateArguments(ArgumentsAccessor argumentsAccessor, ParameterContext parameterContext) throws ArgumentsAggregationException {
        Map<String, String> row = new HashMap<>();
        for (int i = 0; i < COLUMNS.size(); i++) {
            row.put(getFieldName(COLUMNS.get(i)), argumentsAccessor.getString(i));
        }
        return row;
    }

    public static Map<String, FieldMap> getFieldMap() {
        return COLUMNS.stream().collect(Collectors.toMap(
                Function.identity(),
                column -> FieldMap.builder().mapWith(getFieldName(column)).build()
        ));
    }

    private static String getFieldName(String column) {
        return String.format("%s_map", column);
    }
}
