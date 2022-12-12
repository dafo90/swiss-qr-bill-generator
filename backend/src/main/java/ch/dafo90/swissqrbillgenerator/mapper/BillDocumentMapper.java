package ch.dafo90.swissqrbillgenerator.mapper;

import ch.dafo90.swissqrbillgenerator.model.Document;
import ch.dafo90.swissqrbillgenerator.model.PdfBill;
import ch.dafo90.swissqrbillgenerator.model.csv.HeaderMap;
import ch.dafo90.swissqrbillgenerator.model.csv.Row;
import ch.dafo90.swissqrbillgenerator.property.FieldProperties;
import ch.dafo90.swissqrbillgenerator.service.FieldGroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.codecrete.qrbill.canvas.PDFCanvas;
import net.codecrete.qrbill.generator.*;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BillDocumentMapper {

    private final FieldGroupService fieldGroupService;

    public Document toDocument(Row row, List<HeaderMap> headersMap) {
        return Document.builder()

                .senderName(fieldGroupService.getField(FieldProperties.CREDITOR_NAME, row, headersMap))
                .senderStreet(fieldGroupService.getField(FieldProperties.CREDITOR_STREET, row, headersMap))
                .senderLocality(fieldGroupService.getField(FieldProperties.CREDITOR_LOCALITY, row, headersMap))
                .senderCountry(fieldGroupService.getField(FieldProperties.CREDITOR_COUNTRY, row, headersMap))
                .senderEmail(fieldGroupService.getField(FieldProperties.CREDITOR_EMAIL, row, headersMap))
                .senderWebsite(fieldGroupService.getField(FieldProperties.CREDITOR_WEBSITE, row, headersMap))
                .senderPhoneNumber(fieldGroupService.getField(FieldProperties.CREDITOR_PHONE_NUMBER, row, headersMap))

                .recipientTitle(fieldGroupService.getField(FieldProperties.DEBTOR_TITLE, row, headersMap))
                .recipientOrganisation(fieldGroupService.getField(FieldProperties.DEBTOR_ORGANISATION, row, headersMap))
                .recipientName(fieldGroupService.getField(FieldProperties.DEBTOR_NAME, row, headersMap))
                .recipientStreet(fieldGroupService.getField(FieldProperties.DEBTOR_STREET, row, headersMap))
                .recipientLocality(fieldGroupService.getField(FieldProperties.DEBTOR_LOCALITY, row, headersMap))
                .recipientCountry(fieldGroupService.getField(FieldProperties.DEBTOR_COUNTRY, row, headersMap))

                .salutation(fieldGroupService.getField(FieldProperties.SALUTATION, row, headersMap))
                .text(fieldGroupService.getField(FieldProperties.TEXT, row, headersMap))
                .closure(fieldGroupService.getField(FieldProperties.CLOSURE, row, headersMap))
                .signatureTitle1(fieldGroupService.getField(FieldProperties.SIGNATURE_TITLE_1, row, headersMap))
                .signatureName1(fieldGroupService.getField(FieldProperties.SIGNATURE_NAME_1, row, headersMap))
                .signatureTitle2(fieldGroupService.getField(FieldProperties.SIGNATURE_TITLE_2, row, headersMap))
                .signatureName2(fieldGroupService.getField(FieldProperties.SIGNATURE_NAME_2, row, headersMap))

                .build();
    }

    public PdfBill toPdfBill(byte[] pdfDocument, Row row, List<HeaderMap> headersMap) {
        Bill bill = toQrBill(row, headersMap);

        // Append QR bill
        try (PDFCanvas canvas = new PDFCanvas(pdfDocument, PDFCanvas.LAST_PAGE)) {
            QRBill.draw(bill, canvas);
            return PdfBill.builder().pdf(canvas.toByteArray()).fileName(generateBillFileName(bill.getDebtor().getName(), bill.getReference())).build();
        } catch (IOException ex) {
            throw new RuntimeException("Cannot add QR bill to document", ex);
        }
    }

    public Bill toQrBill(Row row, List<HeaderMap> headerMaps) {
        // Setup bill
        Bill bill = new Bill();
        bill.setAccount(fieldGroupService.getField(FieldProperties.ACCOUNT, row, headerMaps));
        bill.setAmountFromDouble(Double.parseDouble(fieldGroupService.getField(FieldProperties.AMOUNT, row, headerMaps)));
        bill.setCurrency(fieldGroupService.getField(FieldProperties.CURRENCY, row, headerMaps));

        // Set creditor
        Address creditor = new Address();
        creditor.setName(fieldGroupService.getField(FieldProperties.CREDITOR_NAME, row, headerMaps));
        creditor.setAddressLine1(fieldGroupService.getField(FieldProperties.CREDITOR_STREET, row, headerMaps));
        creditor.setAddressLine2(fieldGroupService.getField(FieldProperties.CREDITOR_LOCALITY, row, headerMaps));
        creditor.setCountryCode(fieldGroupService.getField(FieldProperties.CREDITOR_COUNTRY, row, headerMaps));
        bill.setCreditor(creditor);

        // More bill data
        bill.setReference(fieldGroupService.getField(FieldProperties.REFERENCE, row, headerMaps));
        bill.setUnstructuredMessage(fieldGroupService.getField(FieldProperties.UNSTRUCTURED_MESSAGE, row, headerMaps));

        // Set debtor
        String debtorName = fieldGroupService.getField(FieldProperties.DEBTOR_NAME, row, headerMaps);
        Address debtor = new Address();
        debtor.setName(debtorName);
        debtor.setAddressLine1(fieldGroupService.getField(FieldProperties.DEBTOR_STREET, row, headerMaps));
        debtor.setAddressLine2(fieldGroupService.getField(FieldProperties.DEBTOR_LOCALITY, row, headerMaps));
        debtor.setCountryCode(fieldGroupService.getField(FieldProperties.DEBTOR_COUNTRY, row, headerMaps));
        bill.setDebtor(debtor);

        // Set output format
        BillFormat format = bill.getFormat();
        format.setGraphicsFormat(GraphicsFormat.PDF);
        format.setOutputSize(OutputSize.QR_BILL_ONLY);
        format.setLanguage(Language.valueOf(fieldGroupService.getField(FieldProperties.LANGUAGE, row, headerMaps)));

        return bill;
    }

    private String generateBillFileName(String debtorName, String reference) {
        if (StringUtils.hasText(reference)) {
            return String.format("%s_%s_%d.pdf", debtorName.replace(" ", "-"), reference, new Date().getTime());
        }
        return String.format("%s_%d.pdf", debtorName.replace(" ", "-"), new Date().getTime());
    }

}
