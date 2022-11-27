package ch.dafo90.swissqrbillgenerator.mapper;

import ch.dafo90.swissqrbillgenerator.exception.ValidationException;
import ch.dafo90.swissqrbillgenerator.model.Document;
import ch.dafo90.swissqrbillgenerator.model.FieldMap;
import ch.dafo90.swissqrbillgenerator.model.FieldType;
import ch.dafo90.swissqrbillgenerator.model.PdfBill;
import ch.dafo90.swissqrbillgenerator.property.AppProperties;
import ch.dafo90.swissqrbillgenerator.property.FieldProperties;
import ch.dafo90.swissqrbillgenerator.property.GroupProperties;
import ch.dafo90.swissqrbillgenerator.util.ValidatorUtils;
import lombok.RequiredArgsConstructor;
import net.codecrete.qrbill.canvas.PDFCanvas;
import net.codecrete.qrbill.generator.*;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
    public static final String DEBTOR_NAME = "debtorName";
    public static final String DEBTOR_STREET = "debtorStreet";
    public static final String DEBTOR_LOCALITY = "debtorLocality";
    public static final String DEBTOR_COUNTRY = "debtorCountry";

    private final AppProperties appProperties;

    public Document toDocument(Map<String, String> row, Map<String, FieldMap> fieldsMap) {
        return Document.builder()

                .senderName(getField(CREDITOR_NAME, row, fieldsMap))
                .senderStreet(getField(CREDITOR_STREET, row, fieldsMap))
                .senderLocality(getField(CREDITOR_LOCALITY, row, fieldsMap))
                .senderCountry(getField(CREDITOR_COUNTRY, row, fieldsMap))
                .senderEmail(getField(CREDITOR_EMAIL, row, fieldsMap))
                .senderWebsite(getField(CREDITOR_WEBSITE, row, fieldsMap))
                .senderPhoneNumber(getField(CREDITOR_PHONE_NUMBER, row, fieldsMap))

                .recipientName(getField(DEBTOR_NAME, row, fieldsMap))
                .recipientStreet(getField(DEBTOR_STREET, row, fieldsMap))
                .recipientLocality(getField(DEBTOR_LOCALITY, row, fieldsMap))
                .recipientCountry(getField(DEBTOR_COUNTRY, row, fieldsMap))

                .salutation(getField(SALUTATION, row, fieldsMap))
                .text(getField(TEXT, row, fieldsMap))
                .closure(getField(CLOSURE, row, fieldsMap))
                .signatureTitle1(getField(SIGNATURE_TITLE_1, row, fieldsMap))
                .signatureName1(getField(SIGNATURE_NAME_1, row, fieldsMap))
                .signatureTitle2(getField(SIGNATURE_TITLE_2, row, fieldsMap))
                .signatureName2(getField(SIGNATURE_NAME_2, row, fieldsMap))

                .build();
    }

    public PdfBill toPdfBill(byte[] pdfDocument, Map<String, String> row, Map<String, FieldMap> fieldsMap) {
        Bill bill = buildBill(row, fieldsMap);

        // Append QR bill
        try (PDFCanvas canvas = new PDFCanvas(pdfDocument, PDFCanvas.LAST_PAGE)) {
            QRBill.draw(bill, canvas);
            return PdfBill.builder().pdf(canvas.toByteArray()).fileName(generateBillFileName(bill.getDebtor().getName(), bill.getReference())).build();
        } catch (IOException ex) {
            throw new RuntimeException("Cannot add QR bill to document", ex);
        }
    }

    protected Bill buildBill(Map<String, String> row, Map<String, FieldMap> fieldsMap) {
        // Setup bill
        Bill bill = new Bill();
        bill.setAccount(getField(ACCOUNT, row, fieldsMap));
        bill.setAmountFromDouble(Double.parseDouble(getField(AMOUNT, row, fieldsMap)));
        bill.setCurrency(getField(CURRENCY, row, fieldsMap));

        // Set creditor
        Address creditor = new Address();
        creditor.setName(getField(CREDITOR_NAME, row, fieldsMap));
        creditor.setAddressLine1(getField(CREDITOR_STREET, row, fieldsMap));
        creditor.setAddressLine2(getField(CREDITOR_LOCALITY, row, fieldsMap));
        creditor.setCountryCode(getField(CREDITOR_COUNTRY, row, fieldsMap));
        bill.setCreditor(creditor);

        // More bill data
        bill.setReference(getField(REFERENCE, row, fieldsMap));
        bill.setUnstructuredMessage(getField(UNSTRUCTURED_MESSAGE, row, fieldsMap));

        // Set debtor
        String debtorName = getField(DEBTOR_NAME, row, fieldsMap);
        Address debtor = new Address();
        debtor.setName(debtorName);
        debtor.setAddressLine1(getField(DEBTOR_STREET, row, fieldsMap));
        debtor.setAddressLine2(getField(DEBTOR_LOCALITY, row, fieldsMap));
        debtor.setCountryCode(getField(DEBTOR_COUNTRY, row, fieldsMap));
        bill.setDebtor(debtor);

        // Set output format
        BillFormat format = bill.getFormat();
        format.setGraphicsFormat(GraphicsFormat.PDF);
        format.setOutputSize(OutputSize.QR_BILL_ONLY);
        format.setLanguage(Language.valueOf(getField(LANGUAGE, row, fieldsMap)));

        return bill;
    }

    protected String generateBillFileName(String debtorName, String reference) {
        if (StringUtils.hasText(reference)) {
            return String.format("%s_%s_%d.pdf", debtorName.replace(" ", "-"), reference, new Date().getTime());
        }
        return String.format("%s_%d.pdf", debtorName.replace(" ", "-"), new Date().getTime());
    }

    protected String getField(String fieldName, Map<String, String> row, Map<String, FieldMap> fieldsMap) {
        FieldMap fieldMap = fieldsMap.get(fieldName);
        if (fieldMap == null) {
            throw new ValidationException(String.format("Field '%s' not mapped", fieldName));
        }
        FieldProperties fieldProperties = getFieldPropertiesByName(fieldName);
        String value = sanitizeValue(getValue(row.get(fieldMap.getMapWith()), fieldMap.getStaticValue(), fieldProperties.defaultValue()), fieldProperties.type());
        ValidatorUtils.validate(fieldName, value, fieldProperties);
        return value;
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
                .map(GroupProperties::fields)
                .flatMap(List::stream)
                .filter(field -> field.name().equals(fieldName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(String.format("Field '%s' not configured in application.yml", fieldName)));
    }

    public String sanitizeValue(String value, FieldType fieldType) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return switch (fieldType) {
            case STRING -> value;
            case TEXT -> value.replace("\n", "<br/>");
            case NUMBER -> value.replaceAll("[^\\d\\.]", "");
            default -> value.replace(" ", "").toUpperCase();
        };
    }

}
