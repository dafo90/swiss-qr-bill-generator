package ch.dafo90.swissqrbillgenerator.mapper;

import ch.dafo90.swissqrbillgenerator.AggregateBillDataRowWithMap;
import ch.dafo90.swissqrbillgenerator.BaseIntegrationTest;
import ch.dafo90.swissqrbillgenerator.ByteChecker;
import ch.dafo90.swissqrbillgenerator.model.Document;
import ch.dafo90.swissqrbillgenerator.model.PdfBill;
import ch.dafo90.swissqrbillgenerator.model.csv.HeaderMap;
import ch.dafo90.swissqrbillgenerator.model.csv.Row;
import ch.dafo90.swissqrbillgenerator.property.AppProperties;
import ch.dafo90.swissqrbillgenerator.property.FieldProperties;
import ch.dafo90.swissqrbillgenerator.service.FieldGroupService;
import net.codecrete.qrbill.generator.Bill;
import net.codecrete.qrbill.generator.GraphicsFormat;
import net.codecrete.qrbill.generator.OutputSize;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.hamcrest.Matchers;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.aggregator.AggregateWith;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class BillDocumentMapperIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private BillDocumentMapper billDocumentMapper;

    @Autowired
    private FieldGroupService fieldGroupService;

    @Autowired
    private AppProperties appProperties;

    @ParameterizedTest
    @CsvFileSource(resources = "/data.csv", numLinesToSkip = 1)
    void toDocument(@AggregateWith(AggregateBillDataRowWithMap.class) Row row) {
        List<HeaderMap> headersMap = AggregateBillDataRowWithMap.getHeadersMap();
        Document document = billDocumentMapper.toDocument(row, headersMap);

        assertEquals(fieldGroupService.getField(FieldProperties.CREDITOR_NAME, row, headersMap), document.getSenderName());
        assertEquals(fieldGroupService.getField(FieldProperties.CREDITOR_STREET, row, headersMap), document.getSenderStreet());
        assertEquals(fieldGroupService.getField(FieldProperties.CREDITOR_LOCALITY, row, headersMap), document.getSenderLocality());
        assertEquals(fieldGroupService.getField(FieldProperties.CREDITOR_COUNTRY, row, headersMap), document.getSenderCountry());
        assertEquals(fieldGroupService.getField(FieldProperties.CREDITOR_EMAIL, row, headersMap), document.getSenderEmail());
        assertEquals(fieldGroupService.getField(FieldProperties.CREDITOR_WEBSITE, row, headersMap), document.getSenderWebsite());
        assertEquals(fieldGroupService.getField(FieldProperties.CREDITOR_PHONE_NUMBER, row, headersMap), document.getSenderPhoneNumber());

        assertEquals(fieldGroupService.getField(FieldProperties.DEBTOR_NAME, row, headersMap), document.getRecipientName());
        assertEquals(fieldGroupService.getField(FieldProperties.DEBTOR_STREET, row, headersMap), document.getRecipientStreet());
        assertEquals(fieldGroupService.getField(FieldProperties.DEBTOR_LOCALITY, row, headersMap), document.getRecipientLocality());
        assertEquals(fieldGroupService.getField(FieldProperties.DEBTOR_COUNTRY, row, headersMap), document.getRecipientCountry());

        assertEquals(fieldGroupService.getField(FieldProperties.SALUTATION, row, headersMap), document.getSalutation());
        assertEquals(fieldGroupService.getField(FieldProperties.TEXT, row, headersMap), document.getText());
        assertEquals(fieldGroupService.getField(FieldProperties.CLOSURE, row, headersMap), document.getClosure());
        assertEquals(fieldGroupService.getField(FieldProperties.SIGNATURE_TITLE_1, row, headersMap), document.getSignatureTitle1());
        assertEquals(fieldGroupService.getField(FieldProperties.SIGNATURE_NAME_1, row, headersMap), document.getSignatureName1());
        assertEquals(fieldGroupService.getField(FieldProperties.SIGNATURE_TITLE_2, row, headersMap), document.getSignatureTitle2());
        assertEquals(fieldGroupService.getField(FieldProperties.SIGNATURE_NAME_2, row, headersMap), document.getSignatureName2());
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
        Bill bill = billDocumentMapper.toQrBill(row, headersMap);

        assertEquals(fieldGroupService.getField(FieldProperties.ACCOUNT, row, headersMap), bill.getAccount());
        assertThat(new BigDecimal(fieldGroupService.getField(FieldProperties.AMOUNT, row, headersMap)), Matchers.comparesEqualTo(bill.getAmount()));
        assertEquals(fieldGroupService.getField(FieldProperties.CURRENCY, row, headersMap), bill.getCurrency());

        assertEquals(fieldGroupService.getField(FieldProperties.CREDITOR_NAME, row, headersMap), bill.getCreditor().getName());
        assertEquals(fieldGroupService.getField(FieldProperties.CREDITOR_STREET, row, headersMap), bill.getCreditor().getAddressLine1());
        assertEquals(fieldGroupService.getField(FieldProperties.CREDITOR_LOCALITY, row, headersMap), bill.getCreditor().getAddressLine2());
        assertEquals(fieldGroupService.getField(FieldProperties.CREDITOR_COUNTRY, row, headersMap), bill.getCreditor().getCountryCode());

        assertEquals(fieldGroupService.getField(FieldProperties.REFERENCE, row, headersMap), bill.getReference());
        assertEquals(fieldGroupService.getField(FieldProperties.UNSTRUCTURED_MESSAGE, row, headersMap), bill.getUnstructuredMessage());

        assertEquals(fieldGroupService.getField(FieldProperties.DEBTOR_NAME, row, headersMap), bill.getDebtor().getName());
        assertEquals(fieldGroupService.getField(FieldProperties.DEBTOR_STREET, row, headersMap), bill.getDebtor().getAddressLine1());
        assertEquals(fieldGroupService.getField(FieldProperties.DEBTOR_LOCALITY, row, headersMap), bill.getDebtor().getAddressLine2());
        assertEquals(fieldGroupService.getField(FieldProperties.DEBTOR_COUNTRY, row, headersMap), bill.getDebtor().getCountryCode());

        assertEquals(GraphicsFormat.PDF, bill.getFormat().getGraphicsFormat());
        assertEquals(OutputSize.QR_BILL_ONLY, bill.getFormat().getOutputSize());
        assertEquals(fieldGroupService.getField(FieldProperties.LANGUAGE, row, headersMap), bill.getFormat().getLanguage().name());
    }

}
