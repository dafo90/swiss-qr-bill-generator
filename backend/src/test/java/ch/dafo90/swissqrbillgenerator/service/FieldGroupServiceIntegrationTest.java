package ch.dafo90.swissqrbillgenerator.service;

import ch.dafo90.swissqrbillgenerator.AggregateBillDataRowWithMap;
import ch.dafo90.swissqrbillgenerator.BaseIntegrationTest;
import ch.dafo90.swissqrbillgenerator.exception.ValidationException;
import ch.dafo90.swissqrbillgenerator.model.FieldType;
import ch.dafo90.swissqrbillgenerator.model.csv.HeaderMap;
import ch.dafo90.swissqrbillgenerator.model.csv.Row;
import ch.dafo90.swissqrbillgenerator.model.validation.ValidationMessage;
import ch.dafo90.swissqrbillgenerator.property.FieldProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.aggregator.AggregateWith;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class FieldGroupServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private FieldGroupService fieldGroupService;

    @ParameterizedTest
    @CsvFileSource(resources = "/data.csv", numLinesToSkip = 1)
    void getField_noFieldMap(@AggregateWith(AggregateBillDataRowWithMap.class) Row row) {
        ValidationException ex = assertThrows(ValidationException.class, () -> fieldGroupService.getField("invalid_field_name", row, AggregateBillDataRowWithMap.getHeadersMap()));
        assertEquals(new ValidationMessage("invalid_field_name", null, "Field 'invalid_field_name' not configured in application.yml", ValidationMessage.INVALID_FIELD_NAME), ex.getMessages().get(0));
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/data.csv", numLinesToSkip = 1)
    void getField_emptyValueGetDefault(@AggregateWith(AggregateBillDataRowWithMap.class) Row row) {
        List<HeaderMap> headersMap = AggregateBillDataRowWithMap.getHeadersMap();

        if (!StringUtils.hasText(row.getCells().get(AggregateBillDataRowWithMap.getIndexOfField(headersMap, FieldProperties.CURRENCY)))) {
            assertEquals("CHF", fieldGroupService.getField(FieldProperties.CURRENCY, row, AggregateBillDataRowWithMap.getHeadersMap()));
        }
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/data.csv", numLinesToSkip = 1)
    void getField_value(@AggregateWith(AggregateBillDataRowWithMap.class) Row row) {
        List<HeaderMap> headersMap = AggregateBillDataRowWithMap.getHeadersMap();

        if (StringUtils.hasText(row.getCells().get(AggregateBillDataRowWithMap.getIndexOfField(headersMap, FieldProperties.LANGUAGE)))) {
            assertEquals("RM", fieldGroupService.getField(FieldProperties.LANGUAGE, row, AggregateBillDataRowWithMap.getHeadersMap()));
        }
    }

    @Test
    void getValue() {
        assertNull(fieldGroupService.getValue(null, null, null));
        assertEquals("defaultValue", fieldGroupService.getValue(null, null, "defaultValue"));
        assertEquals("staticValue", fieldGroupService.getValue(null, "staticValue", null));
        assertEquals("staticValue", fieldGroupService.getValue(null, "staticValue", "defaultValue"));
        assertEquals("value", fieldGroupService.getValue("value", null, null));
        assertEquals("value", fieldGroupService.getValue("value", null, "defaultValue"));
        assertEquals("value", fieldGroupService.getValue("value", "staticValue", null));
        assertEquals("value", fieldGroupService.getValue("value", "staticValue", "defaultValue"));
    }

    @ParameterizedTest
    @EnumSource(value = FieldType.class, names = {"STRING", "EMAIL", "URL"})
    void sanitizeValue_returnSameValue(FieldType fieldType) {
        String value = faker.howIMetYourMother().quote();
        assertEquals(value, fieldGroupService.sanitizeValue(value, fieldType));
    }

    @ParameterizedTest
    @EnumSource(value = FieldType.class, names = {"IBAN", "REFERENCE", "CURRENCY_CODE", "LANGUAGE_CODE", "COUNTRY_CODE"})
    void sanitizeValue_removeSpaces(FieldType fieldType) {
        String value = faker.address().fullAddress();
        assertEquals(value.replace(" ", "").toUpperCase(), fieldGroupService.sanitizeValue(value, fieldType));
    }

    @ParameterizedTest
    @EnumSource(value = FieldType.class, names = {"TEXT"})
    void sanitizeValue_replaceNewLineWithBrTag(FieldType fieldType) {
        String value = Stream.of(
                faker.howIMetYourMother().quote(),
                faker.howIMetYourMother().quote(),
                faker.howIMetYourMother().quote()
        ).collect(Collectors.joining("\n"));

        assertEquals(value.replace("\n", "<br/>"), fieldGroupService.sanitizeValue(value, fieldType));
    }

    @ParameterizedTest
    @EnumSource(value = FieldType.class, names = {"NUMBER"})
    void sanitizeValue_replaceAllExceptNumbersAndPeriods(FieldType fieldType) {
        String value = String.format("-%s", faker.space().distanceMeasurement());
        assertEquals(value.replaceAll("[^\\d\\.-]", ""), fieldGroupService.sanitizeValue(value, fieldType));
    }

}
