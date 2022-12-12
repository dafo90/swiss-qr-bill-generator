package ch.dafo90.swissqrbillgenerator.service;

import ch.dafo90.swissqrbillgenerator.exception.ValidationException;
import ch.dafo90.swissqrbillgenerator.mapper.BillDocumentMapper;
import ch.dafo90.swissqrbillgenerator.model.*;
import ch.dafo90.swissqrbillgenerator.model.csv.HeaderMap;
import ch.dafo90.swissqrbillgenerator.model.csv.Row;
import ch.dafo90.swissqrbillgenerator.model.validation.ValidationResult;
import ch.dafo90.swissqrbillgenerator.util.HtmlToPdfBuilder;
import ch.dafo90.swissqrbillgenerator.util.PdfToJpgBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class BillPdfGeneratorService {

    private final BillDocumentMapper billDocumentMapper;
    private final TemplateEngine pdfTemplateEngine;
    private final ValidationService validationService;

    public byte[] generateZippedBills(BillsData billsData) {
        ValidationResult validationResult = validationService.validate(billsData);
        if (!validationResult.isValid()) {
            throw new ValidationException(validationResult.getValidationMessages());
        }
        Base64Image logo = Base64Image.of(billsData.getLogoBase64());
        return prepareZip(
                billsData.getData()
                        .stream()
                        .map(row -> generateQrBillPdf(row, logo, billsData.getHeadersMap()))
                        .toList()
        );
    }

    public byte[] generateJpgPreview(BillData billData) {
        ValidationResult validationResult = validationService.validate(billData);
        if (!validationResult.isValid()) {
            throw new ValidationException(validationResult.getValidationMessages());
        }
        Base64Image logo = Base64Image.of(billData.getLogoBase64());
        Document document = billDocumentMapper.toDocument(billData.getData(), billData.getHeadersMap());
        log.info("Generate preview bill for {}", document.getRecipientName());
        return PdfToJpgBuilder.convertToJpg(HtmlToPdfBuilder.convertToPdf(loadBillTemplate(logo, document)));
    }

    private PdfBill generateQrBillPdf(Row row, Base64Image logo, List<HeaderMap> headersMap) {
        Document document = billDocumentMapper.toDocument(row, headersMap);
        log.info("Generate bill for {}", document.getRecipientName());
        byte[] pdfDocument = HtmlToPdfBuilder.convertToPdf(loadBillTemplate(logo, document));
        return billDocumentMapper.toPdfBill(pdfDocument, row, headersMap);
    }

    private String loadBillTemplate(Base64Image logo, Document document) {
        Context ctx = new Context();
        ctx.setVariable("document", document);
        ctx.setVariable("logoBase64", logo.getDataUrl());
        return pdfTemplateEngine.process("bill-document", ctx);
    }

    protected byte[] prepareZip(List<PdfBill> pdfBills) {
        log.info("Preparing ZIP file for {} bills", pdfBills.size());
        try (
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                ZipOutputStream zout = new ZipOutputStream(bout)
        ) {
            for (PdfBill pdfBill : pdfBills) {
                ZipEntry ze = new ZipEntry(pdfBill.getFileName());
                ze.setSize(pdfBill.getPdf().length);
                zout.putNextEntry(ze);
                zout.write(pdfBill.getPdf());
            }
            zout.finish();
            zout.closeEntry();
            return bout.toByteArray();
        } catch (IOException ex) {
            throw new RuntimeException("Cannot prepare ZIP", ex);
        }
    }

}
