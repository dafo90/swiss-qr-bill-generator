package ch.dafo90.swissqrbillgenerator.mapper;

import ch.dafo90.swissqrbillgenerator.AggregateBillDataRowWithMap;
import ch.dafo90.swissqrbillgenerator.BaseIntegrationTest;
import ch.dafo90.swissqrbillgenerator.exception.ValidationException;
import ch.dafo90.swissqrbillgenerator.model.Document;
import ch.dafo90.swissqrbillgenerator.model.FieldMap;
import ch.dafo90.swissqrbillgenerator.model.FieldType;
import ch.dafo90.swissqrbillgenerator.model.PdfBill;
import net.codecrete.qrbill.generator.Bill;
import net.codecrete.qrbill.generator.GraphicsFormat;
import net.codecrete.qrbill.generator.OutputSize;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.aggregator.AggregateWith;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class BillDocumentMapperIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private BillDocumentMapper billDocumentMapper;

    @ParameterizedTest
    @CsvFileSource(resources = "/data.csv", numLinesToSkip = 1)
    void toDocument(@AggregateWith(AggregateBillDataRowWithMap.class) Map<String, String> row) {
        Map<String, FieldMap> fieldMap = AggregateBillDataRowWithMap.getFieldMap();
        Document document = billDocumentMapper.toDocument(row, fieldMap);

        assertEquals(billDocumentMapper.getField(BillDocumentMapper.CREDITOR_NAME, row, fieldMap), document.getSenderName());
        assertEquals(billDocumentMapper.getField(BillDocumentMapper.CREDITOR_STREET, row, fieldMap), document.getSenderStreet());
        assertEquals(billDocumentMapper.getField(BillDocumentMapper.CREDITOR_LOCALITY, row, fieldMap), document.getSenderLocality());
        assertEquals(billDocumentMapper.getField(BillDocumentMapper.CREDITOR_COUNTRY, row, fieldMap), document.getSenderCountry());
        assertEquals(billDocumentMapper.getField(BillDocumentMapper.CREDITOR_EMAIL, row, fieldMap), document.getSenderEmail());
        assertEquals(billDocumentMapper.getField(BillDocumentMapper.CREDITOR_WEBSITE, row, fieldMap), document.getSenderWebsite());
        assertEquals(billDocumentMapper.getField(BillDocumentMapper.CREDITOR_PHONE_NUMBER, row, fieldMap), document.getSenderPhoneNumber());

        assertEquals(billDocumentMapper.getField(BillDocumentMapper.DEBTOR_NAME, row, fieldMap), document.getRecipientName());
        assertEquals(billDocumentMapper.getField(BillDocumentMapper.DEBTOR_STREET, row, fieldMap), document.getRecipientStreet());
        assertEquals(billDocumentMapper.getField(BillDocumentMapper.DEBTOR_LOCALITY, row, fieldMap), document.getRecipientLocality());
        assertEquals(billDocumentMapper.getField(BillDocumentMapper.DEBTOR_COUNTRY, row, fieldMap), document.getRecipientCountry());

        assertEquals(billDocumentMapper.getField(BillDocumentMapper.SALUTATION, row, fieldMap), document.getSalutation());
        assertEquals(billDocumentMapper.getField(BillDocumentMapper.TEXT, row, fieldMap), document.getText());
        assertEquals(billDocumentMapper.getField(BillDocumentMapper.CLOSURE, row, fieldMap), document.getClosure());
        assertEquals(billDocumentMapper.getField(BillDocumentMapper.SIGNATURE_TITLE_1, row, fieldMap), document.getSignatureTitle1());
        assertEquals(billDocumentMapper.getField(BillDocumentMapper.SIGNATURE_NAME_1, row, fieldMap), document.getSignatureName1());
        assertEquals(billDocumentMapper.getField(BillDocumentMapper.SIGNATURE_TITLE_2, row, fieldMap), document.getSignatureTitle2());
        assertEquals(billDocumentMapper.getField(BillDocumentMapper.SIGNATURE_NAME_2, row, fieldMap), document.getSignatureName2());
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/data.csv", numLinesToSkip = 1)
    void toBillPdf(@AggregateWith(AggregateBillDataRowWithMap.class) Map<String, String> row) throws IOException {
        Map<String, FieldMap> fieldMap = AggregateBillDataRowWithMap.getFieldMap();

        // Creating PDF document object
        PDDocument document = new PDDocument();

        // Add an empty page to it
        document.addPage(new PDPage());

        // Create PDF
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        document.save(os);
        document.close();
        byte[] emptyPdf = os.toByteArray();

        PdfBill pdfBill = billDocumentMapper.toPdfBill(emptyPdf, row, AggregateBillDataRowWithMap.getFieldMap());
        assertNotNull(pdfBill);
        assertEquals(billDocumentMapper.generateBillFileName(
                billDocumentMapper.getField(BillDocumentMapper.DEBTOR_NAME, row, fieldMap),
                billDocumentMapper.getField(BillDocumentMapper.REFERENCE, row, fieldMap)
        ), pdfBill.getFileName());
        assertNotNull(pdfBill.getPdf());
        assertTrue(pdfBill.getPdf().length > 0);
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/data.csv", numLinesToSkip = 1)
    void buildBill(@AggregateWith(AggregateBillDataRowWithMap.class) Map<String, String> row) {
        Map<String, FieldMap> fieldMap = AggregateBillDataRowWithMap.getFieldMap();
        Bill bill = billDocumentMapper.buildBill(row, fieldMap);

        assertEquals(billDocumentMapper.getField(BillDocumentMapper.ACCOUNT, row, fieldMap), bill.getAccount());
        assertThat(new BigDecimal(billDocumentMapper.getField(BillDocumentMapper.AMOUNT, row, fieldMap)), Matchers.comparesEqualTo(bill.getAmount()));
        assertEquals(billDocumentMapper.getField(BillDocumentMapper.CURRENCY, row, fieldMap), bill.getCurrency());

        assertEquals(billDocumentMapper.getField(BillDocumentMapper.CREDITOR_NAME, row, fieldMap), bill.getCreditor().getName());
        assertEquals(billDocumentMapper.getField(BillDocumentMapper.CREDITOR_STREET, row, fieldMap), bill.getCreditor().getAddressLine1());
        assertEquals(billDocumentMapper.getField(BillDocumentMapper.CREDITOR_LOCALITY, row, fieldMap), bill.getCreditor().getAddressLine2());
        assertEquals(billDocumentMapper.getField(BillDocumentMapper.CREDITOR_COUNTRY, row, fieldMap), bill.getCreditor().getCountryCode());

        assertEquals(billDocumentMapper.getField(BillDocumentMapper.REFERENCE, row, fieldMap), bill.getReference());
        assertEquals(billDocumentMapper.getField(BillDocumentMapper.UNSTRUCTURED_MESSAGE, row, fieldMap), bill.getUnstructuredMessage());

        assertEquals(billDocumentMapper.getField(BillDocumentMapper.DEBTOR_NAME, row, fieldMap), bill.getDebtor().getName());
        assertEquals(billDocumentMapper.getField(BillDocumentMapper.DEBTOR_STREET, row, fieldMap), bill.getDebtor().getAddressLine1());
        assertEquals(billDocumentMapper.getField(BillDocumentMapper.DEBTOR_LOCALITY, row, fieldMap), bill.getDebtor().getAddressLine2());
        assertEquals(billDocumentMapper.getField(BillDocumentMapper.DEBTOR_COUNTRY, row, fieldMap), bill.getDebtor().getCountryCode());

        assertEquals(GraphicsFormat.PDF, bill.getFormat().getGraphicsFormat());
        assertEquals(OutputSize.QR_BILL_ONLY, bill.getFormat().getOutputSize());
        assertEquals(billDocumentMapper.getField(BillDocumentMapper.LANGUAGE, row, fieldMap), bill.getFormat().getLanguage().name());
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/data.csv", numLinesToSkip = 1)
    void getField_trowValidationExceptionDueNoFieldMap(@AggregateWith(AggregateBillDataRowWithMap.class) Map<String, String> row) {
        ValidationException ex = assertThrows(ValidationException.class, () -> billDocumentMapper.getField("invalid_field_name", row, AggregateBillDataRowWithMap.getFieldMap()));
        assertEquals("Field 'invalid_field_name' not mapped", ex.getMessage());
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/data.csv", numLinesToSkip = 1)
    void getField_emptyValueGetDefault(@AggregateWith(AggregateBillDataRowWithMap.class) Map<String, String> row) {
        if (!StringUtils.hasText(row.get(BillDocumentMapper.CURRENCY))) {
            assertEquals("CHF", billDocumentMapper.getField(BillDocumentMapper.CURRENCY, row, AggregateBillDataRowWithMap.getFieldMap()));
        }
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/data.csv", numLinesToSkip = 1)
    void getField_value(@AggregateWith(AggregateBillDataRowWithMap.class) Map<String, String> row) {
        if (StringUtils.hasText(row.get(BillDocumentMapper.LANGUAGE))) {
            assertEquals("RM", billDocumentMapper.getField(BillDocumentMapper.LANGUAGE, row, AggregateBillDataRowWithMap.getFieldMap()));
        }
    }

    @Test
    void getValue() {
        assertNull(billDocumentMapper.getValue(null, null, null));
        assertEquals("defaultValue", billDocumentMapper.getValue(null, null, "defaultValue"));
        assertEquals("staticValue", billDocumentMapper.getValue(null, "staticValue", null));
        assertEquals("staticValue", billDocumentMapper.getValue(null, "staticValue", "defaultValue"));
        assertEquals("value", billDocumentMapper.getValue("value", null, null));
        assertEquals("value", billDocumentMapper.getValue("value", null, "defaultValue"));
        assertEquals("value", billDocumentMapper.getValue("value", "staticValue", null));
        assertEquals("value", billDocumentMapper.getValue("value", "staticValue", "defaultValue"));
    }

    @ParameterizedTest
    @EnumSource(value = FieldType.class, names = {"STRING"})
    void sanitizeValue_returnSameValue(FieldType fieldType) {
        String value = faker.howIMetYourMother().quote();
        assertEquals(value, billDocumentMapper.sanitizeValue(value, fieldType));
    }

    @ParameterizedTest
    @EnumSource(value = FieldType.class, names = {"IBAN", "REFERENCE", "EMAIL", "URL", "CURRENCY_CODE", "LANGUAGE_CODE", "COUNTRY_CODE"})
    void sanitizeValue_removeSpaces(FieldType fieldType) {
        String value = faker.address().fullAddress();
        assertEquals(value.replace(" ", "").toUpperCase(), billDocumentMapper.sanitizeValue(value, fieldType));
    }

    @ParameterizedTest
    @EnumSource(value = FieldType.class, names = {"TEXT"})
    void sanitizeValue_replaceNewLineWithBrTag(FieldType fieldType) {
        String value = Stream.of(
                faker.howIMetYourMother().quote(),
                faker.howIMetYourMother().quote(),
                faker.howIMetYourMother().quote()
        ).collect(Collectors.joining("\n"));

        assertEquals(value.replace("\n", "<br/>"), billDocumentMapper.sanitizeValue(value, fieldType));
    }

    @ParameterizedTest
    @EnumSource(value = FieldType.class, names = {"NUMBER"})
    void sanitizeValue_replaceAllExceptNumbersAndPeriods(FieldType fieldType) {
        String value = faker.space().distanceMeasurement();
        assertEquals(value.replaceAll("[^\\d\\.]", ""), billDocumentMapper.sanitizeValue(value, fieldType));
    }

}
