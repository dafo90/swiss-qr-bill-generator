package ch.dafo90.swissqrbillgenerator.mapper;

import ch.dafo90.swissqrbillgenerator.exception.ValidationException;
import ch.dafo90.swissqrbillgenerator.model.Document;
import ch.dafo90.swissqrbillgenerator.model.FieldType;
import ch.dafo90.swissqrbillgenerator.model.PdfBill;
import ch.dafo90.swissqrbillgenerator.model.csv.HeaderMap;
import ch.dafo90.swissqrbillgenerator.model.csv.Row;
import ch.dafo90.swissqrbillgenerator.property.AppProperties;
import ch.dafo90.swissqrbillgenerator.property.FieldGroupProperties;
import ch.dafo90.swissqrbillgenerator.property.FieldProperties;
import ch.dafo90.swissqrbillgenerator.util.ValidatorUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.codecrete.qrbill.canvas.PDFCanvas;
import net.codecrete.qrbill.generator.*;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class BillDocumentMapper {

    // Document
    public static final String SALUTATION = "salutation";
    public static final String TEXT = "text";
    public static final String SIGNATURE_TITLE_1 = "signatureTitle1";
    public static final String SIGNATURE_NAME_1 = "signatureName1";
    public static final String SIGNATURE_TITLE_2 = "signatureTitle2";
    public static final String SIGNATURE_NAME_2 = "signatureName2";
    public static final String CLOSURE = "closure";

    // Bill
    public static final String ACCOUNT = "account";
    public static final String AMOUNT = "amount";
    public static final String CURRENCY = "currency";
    public static final String LANGUAGE = "language";
    public static final String REFERENCE = "reference";
    public static final String UNSTRUCTURED_MESSAGE = "unstructuredMessage";

    // Creditor
    public static final String CREDITOR_NAME = "creditorName";
    public static final String CREDITOR_STREET = "creditorStreet";
    public static final String CREDITOR_LOCALITY = "creditorLocality";
    public static final String CREDITOR_COUNTRY = "creditorCountry";
    public static final String CREDITOR_EMAIL = "creditorEmail";
    public static final String CREDITOR_WEBSITE = "creditorWebsite";
    public static final String CREDITOR_PHONE_NUMBER = "creditorPhoneNumber";

    // Debtor
    public static final String DEBTOR_TITLE = "debtorTitle";
    public static final String DEBTOR_ORGANISATION = "debtorOrganisation";
    public static final String DEBTOR_NAME = "debtorName";
    public static final String DEBTOR_STREET = "debtorStreet";
    public static final String DEBTOR_LOCALITY = "debtorLocality";
    public static final String DEBTOR_COUNTRY = "debtorCountry";

    private final AppProperties appProperties;

    public Document toDocument(Row row, List<HeaderMap> headersMap) {
        return Document.builder()

                .senderName(getField(CREDITOR_NAME, row, headersMap))
                .senderStreet(getField(CREDITOR_STREET, row, headersMap))
                .senderLocality(getField(CREDITOR_LOCALITY, row, headersMap))
                .senderCountry(getField(CREDITOR_COUNTRY, row, headersMap))
                .senderEmail(getField(CREDITOR_EMAIL, row, headersMap))
                .senderWebsite(getField(CREDITOR_WEBSITE, row, headersMap))
                .senderPhoneNumber(getField(CREDITOR_PHONE_NUMBER, row, headersMap))

                .recipientTitle(getField(DEBTOR_TITLE, row, headersMap))
                .recipientOrganisation(getField(DEBTOR_ORGANISATION, row, headersMap))
                .recipientName(getField(DEBTOR_NAME, row, headersMap))
                .recipientStreet(getField(DEBTOR_STREET, row, headersMap))
                .recipientLocality(getField(DEBTOR_LOCALITY, row, headersMap))
                .recipientCountry(getField(DEBTOR_COUNTRY, row, headersMap))

                .salutation(getField(SALUTATION, row, headersMap))
                .text(getField(TEXT, row, headersMap))
                .closure(getField(CLOSURE, row, headersMap))
                .signatureTitle1(getField(SIGNATURE_TITLE_1, row, headersMap))
                .signatureName1(getField(SIGNATURE_NAME_1, row, headersMap))
                .signatureTitle2(getField(SIGNATURE_TITLE_2, row, headersMap))
                .signatureName2(getField(SIGNATURE_NAME_2, row, headersMap))

                .build();
    }

    public PdfBill toPdfBill(byte[] pdfDocument, Row row, List<HeaderMap> headersMap) {
        Bill bill = buildBill(row, headersMap);

        // Append QR bill
        try (PDFCanvas canvas = new PDFCanvas(pdfDocument, PDFCanvas.LAST_PAGE)) {
            QRBill.draw(bill, canvas);
            return PdfBill.builder().pdf(canvas.toByteArray()).fileName(generateBillFileName(bill.getDebtor().getName(), bill.getReference())).build();
        } catch (IOException ex) {
            throw new RuntimeException("Cannot add QR bill to document", ex);
        }
    }

    protected Bill buildBill(Row row, List<HeaderMap> headerMaps) {
        // Setup bill
        Bill bill = new Bill();
        bill.setAccount(getField(ACCOUNT, row, headerMaps));
        bill.setAmountFromDouble(Double.parseDouble(getField(AMOUNT, row, headerMaps)));
        bill.setCurrency(getField(CURRENCY, row, headerMaps));

        // Set creditor
        Address creditor = new Address();
        creditor.setName(getField(CREDITOR_NAME, row, headerMaps));
        creditor.setAddressLine1(getField(CREDITOR_STREET, row, headerMaps));
        creditor.setAddressLine2(getField(CREDITOR_LOCALITY, row, headerMaps));
        creditor.setCountryCode(getField(CREDITOR_COUNTRY, row, headerMaps));
        bill.setCreditor(creditor);

        // More bill data
        bill.setReference(getField(REFERENCE, row, headerMaps));
        bill.setUnstructuredMessage(getField(UNSTRUCTURED_MESSAGE, row, headerMaps));

        // Set debtor
        String debtorName = getField(DEBTOR_NAME, row, headerMaps);
        Address debtor = new Address();
        debtor.setName(debtorName);
        debtor.setAddressLine1(getField(DEBTOR_STREET, row, headerMaps));
        debtor.setAddressLine2(getField(DEBTOR_LOCALITY, row, headerMaps));
        debtor.setCountryCode(getField(DEBTOR_COUNTRY, row, headerMaps));
        bill.setDebtor(debtor);

        // Set output format
        BillFormat format = bill.getFormat();
        format.setGraphicsFormat(GraphicsFormat.PDF);
        format.setOutputSize(OutputSize.QR_BILL_ONLY);
        format.setLanguage(Language.valueOf(getField(LANGUAGE, row, headerMaps)));

        return bill;
    }

    private String generateBillFileName(String debtorName, String reference) {
        if (StringUtils.hasText(reference)) {
            return String.format("%s_%s_%d.pdf", debtorName.replace(" ", "-"), reference, new Date().getTime());
        }
        return String.format("%s_%d.pdf", debtorName.replace(" ", "-"), new Date().getTime());
    }

    protected String getField(String fieldName, Row row, List<HeaderMap> headersMap) {
        FieldProperties fieldProperties = getFieldPropertiesByName(fieldName);
        Optional<HeaderMap> headerMapOptional = getHeaderMap(headersMap, fieldName);

        return headerMapOptional.map(headerMap -> {
            String value = sanitizeValue(getValue(row.getCells().get(headerMap.getMapWithIndex()), headerMap.getStaticValue(), fieldProperties.defaultValue()), fieldProperties.type());
            ValidatorUtils.validate(fieldName, value, fieldProperties);
            return value;
        }).orElseGet(() -> {
            log.debug("Field '{}' not mapped", fieldName);
            String value = getValue(null, null, fieldProperties.defaultValue());
            ValidatorUtils.validate(fieldName, value, fieldProperties);
            return value;
        });
    }

    private Optional<HeaderMap> getHeaderMap(List<HeaderMap> headersMap, String fieldName) {
        return headersMap.stream().filter(headerMap -> headerMap.getFieldName().equals(fieldName)).findFirst();
    }

    protected String getValue(String value, String staticValue, String defaultValue) {
        if (StringUtils.hasText(value)) {
            return value.trim();
        }
        if (StringUtils.hasText(staticValue)) {
            return staticValue.trim();
        }
        if (StringUtils.hasText(defaultValue)) {
            return defaultValue.trim();
        }
        return null;
    }

    private FieldProperties getFieldPropertiesByName(String fieldName) {
        return appProperties.groups().stream()
                .map(FieldGroupProperties::fields)
                .flatMap(List::stream)
                .filter(field -> field.name().equals(fieldName))
                .findFirst()
                .orElseThrow(() -> new ValidationException(String.format("Field '%s' not configured in application.yml", fieldName)));
    }

    protected String sanitizeValue(String value, FieldType fieldType) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return switch (fieldType) {
            case STRING, URL, EMAIL -> value;
            case TEXT -> value.replace("\n", "<br/>");
            case NUMBER -> value.replaceAll("[^\\d\\.-]", "");
            default -> value.replace(" ", "").toUpperCase();
        };
    }

}
