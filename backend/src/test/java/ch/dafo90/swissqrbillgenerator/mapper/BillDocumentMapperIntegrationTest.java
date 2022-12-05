package ch.dafo90.swissqrbillgenerator.mapper;

import ch.dafo90.swissqrbillgenerator.AggregateBillDataRowWithMap;
import ch.dafo90.swissqrbillgenerator.BaseIntegrationTest;
import ch.dafo90.swissqrbillgenerator.ByteChecker;
import ch.dafo90.swissqrbillgenerator.model.Document;
import ch.dafo90.swissqrbillgenerator.model.FieldType;
import ch.dafo90.swissqrbillgenerator.model.PdfBill;
import ch.dafo90.swissqrbillgenerator.model.csv.HeaderMap;
import ch.dafo90.swissqrbillgenerator.model.csv.Row;
import ch.dafo90.swissqrbillgenerator.property.AppProperties;
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
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class BillDocumentMapperIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private BillDocumentMapper billDocumentMapper;

    @Autowired
    private AppProperties appProperties;

    @ParameterizedTest
    @CsvFileSource(resources = "/data.csv", numLinesToSkip = 1)
    void toDocument(@AggregateWith(AggregateBillDataRowWithMap.class) Row row) {
        List<HeaderMap> headersMap = AggregateBillDataRowWithMap.getHeadersMap();
        Document document = billDocumentMapper.toDocument(row, headersMap);

        assertEquals(billDocumentMapper.getField(BillDocumentMapper.CREDITOR_NAME, row, headersMap), document.getSenderName());
        assertEquals(billDocumentMapper.getField(BillDocumentMapper.CREDITOR_STREET, row, headersMap), document.getSenderStreet());
        assertEquals(billDocumentMapper.getField(BillDocumentMapper.CREDITOR_LOCALITY, row, headersMap), document.getSenderLocality());
        assertEquals(billDocumentMapper.getField(BillDocumentMapper.CREDITOR_COUNTRY, row, headersMap), document.getSenderCountry());
        assertEquals(billDocumentMapper.getField(BillDocumentMapper.CREDITOR_EMAIL, row, headersMap), document.getSenderEmail());
        assertEquals(billDocumentMapper.getField(BillDocumentMapper.CREDITOR_WEBSITE, row, headersMap), document.getSenderWebsite());
        assertEquals(billDocumentMapper.getField(BillDocumentMapper.CREDITOR_PHONE_NUMBER, row, headersMap), document.getSenderPhoneNumber());

        assertEquals(billDocumentMapper.getField(BillDocumentMapper.DEBTOR_NAME, row, headersMap), document.getRecipientName());
        assertEquals(billDocumentMapper.getField(BillDocumentMapper.DEBTOR_STREET, row, headersMap), document.getRecipientStreet());
        assertEquals(billDocumentMapper.getField(BillDocumentMapper.DEBTOR_LOCALITY, row, headersMap), document.getRecipientLocality());
        assertEquals(billDocumentMapper.getField(BillDocumentMapper.DEBTOR_COUNTRY, row, headersMap), document.getRecipientCountry());

        assertEquals(billDocumentMapper.getField(BillDocumentMapper.SALUTATION, row, headersMap), document.getSalutation());
        assertEquals(billDocumentMapper.getField(BillDocumentMapper.TEXT, row, headersMap), document.getText());
        assertEquals(billDocumentMapper.getField(BillDocumentMapper.CLOSURE, row, headersMap), document.getClosure());
        assertEquals(billDocumentMapper.getField(BillDocumentMapper.SIGNATURE_TITLE_1, row, headersMap), document.getSignatureTitle1());
        assertEquals(billDocumentMapper.getField(BillDocumentMapper.SIGNATURE_NAME_1, row, headersMap), document.getSignatureName1());
        assertEquals(billDocumentMapper.getField(BillDocumentMapper.SIGNATURE_TITLE_2, row, headersMap), document.getSignatureTitle2());
        assertEquals(billDocumentMapper.getField(BillDocumentMapper.SIGNATURE_NAME_2, row, headersMap), document.getSignatureName2());
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/data.csv", numLinesToSkip = 1)
    void toBillPdf(@AggregateWith(AggregateBillDataRowWithMap.class) Row row) throws IOException {
        // Creating PDF document object
        PDDocument document = new PDDocument();

        // Add an empty page to it
        document.addPage(new PDPage());

        // Create PDF
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        document.save(os);
        document.close();
        byte[] emptyPdf = os.toByteArray();

        PdfBill pdfBill = billDocumentMapper.toPdfBill(emptyPdf, row, AggregateBillDataRowWithMap.getHeadersMap());
        assertNotNull(pdfBill);
        assertNotNull(pdfBill.getFileName());
        assertTrue(pdfBill.getFileName().endsWith(".pdf"));
        assertTrue(ByteChecker.isValidPdf(pdfBill.getPdf()));
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/data.csv", numLinesToSkip = 1)
    void buildBill(@AggregateWith(AggregateBillDataRowWithMap.class) Row row) {
        List<HeaderMap> headersMap = AggregateBillDataRowWithMap.getHeadersMap();
        Bill bill = billDocumentMapper.buildBill(row, headersMap);

        assertEquals(billDocumentMapper.getField(BillDocumentMapper.ACCOUNT, row, headersMap), bill.getAccount());
        assertThat(new BigDecimal(billDocumentMapper.getField(BillDocumentMapper.AMOUNT, row, headersMap)), Matchers.comparesEqualTo(bill.getAmount()));
        assertEquals(billDocumentMapper.getField(BillDocumentMapper.CURRENCY, row, headersMap), bill.getCurrency());

        assertEquals(billDocumentMapper.getField(BillDocumentMapper.CREDITOR_NAME, row, headersMap), bill.getCreditor().getName());
        assertEquals(billDocumentMapper.getField(BillDocumentMapper.CREDITOR_STREET, row, headersMap), bill.getCreditor().getAddressLine1());
        assertEquals(billDocumentMapper.getField(BillDocumentMapper.CREDITOR_LOCALITY, row, headersMap), bill.getCreditor().getAddressLine2());
        assertEquals(billDocumentMapper.getField(BillDocumentMapper.CREDITOR_COUNTRY, row, headersMap), bill.getCreditor().getCountryCode());

        assertEquals(billDocumentMapper.getField(BillDocumentMapper.REFERENCE, row, headersMap), bill.getReference());
        assertEquals(billDocumentMapper.getField(BillDocumentMapper.UNSTRUCTURED_MESSAGE, row, headersMap), bill.getUnstructuredMessage());

        assertEquals(billDocumentMapper.getField(BillDocumentMapper.DEBTOR_NAME, row, headersMap), bill.getDebtor().getName());
        assertEquals(billDocumentMapper.getField(BillDocumentMapper.DEBTOR_STREET, row, headersMap), bill.getDebtor().getAddressLine1());
        assertEquals(billDocumentMapper.getField(BillDocumentMapper.DEBTOR_LOCALITY, row, headersMap), bill.getDebtor().getAddressLine2());
        assertEquals(billDocumentMapper.getField(BillDocumentMapper.DEBTOR_COUNTRY, row, headersMap), bill.getDebtor().getCountryCode());

        assertEquals(GraphicsFormat.PDF, bill.getFormat().getGraphicsFormat());
        assertEquals(OutputSize.QR_BILL_ONLY, bill.getFormat().getOutputSize());
        assertEquals(billDocumentMapper.getField(BillDocumentMapper.LANGUAGE, row, headersMap), bill.getFormat().getLanguage().name());
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/data.csv", numLinesToSkip = 1)
    void getField_trowValidationExceptionDueNoFieldMap(@AggregateWith(AggregateBillDataRowWithMap.class) Row row) {
        RuntimeException ex = assertThrows(RuntimeException.class, () -> billDocumentMapper.getField("invalid_field_name", row, AggregateBillDataRowWithMap.getHeadersMap()));
        assertEquals("Field 'invalid_field_name' not configured in application.yml", ex.getMessage());
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/data.csv", numLinesToSkip = 1)
    void getField_emptyValueGetDefault(@AggregateWith(AggregateBillDataRowWithMap.class) Row row) {
        List<HeaderMap> headersMap = AggregateBillDataRowWithMap.getHeadersMap();

        if (!StringUtils.hasText(row.getCells().get(AggregateBillDataRowWithMap.getIndexOfField(headersMap, BillDocumentMapper.CURRENCY)))) {
            assertEquals("CHF", billDocumentMapper.getField(BillDocumentMapper.CURRENCY, row, AggregateBillDataRowWithMap.getHeadersMap()));
        }
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/data.csv", numLinesToSkip = 1)
    void getField_value(@AggregateWith(AggregateBillDataRowWithMap.class) Row row) {
        List<HeaderMap> headersMap = AggregateBillDataRowWithMap.getHeadersMap();

        if (StringUtils.hasText(row.getCells().get(AggregateBillDataRowWithMap.getIndexOfField(headersMap, BillDocumentMapper.LANGUAGE)))) {
            assertEquals("RM", billDocumentMapper.getField(BillDocumentMapper.LANGUAGE, row, AggregateBillDataRowWithMap.getHeadersMap()));
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
    @EnumSource(value = FieldType.class, names = {"STRING", "EMAIL", "URL"})
    void sanitizeValue_returnSameValue(FieldType fieldType) {
        String value = faker.howIMetYourMother().quote();
        assertEquals(value, billDocumentMapper.sanitizeValue(value, fieldType));
    }

    @ParameterizedTest
    @EnumSource(value = FieldType.class, names = {"IBAN", "REFERENCE", "CURRENCY_CODE", "LANGUAGE_CODE", "COUNTRY_CODE"})
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
        String value = String.format("-%s", faker.space().distanceMeasurement());
        assertEquals(value.replaceAll("[^\\d\\.-]", ""), billDocumentMapper.sanitizeValue(value, fieldType));
    }

}
