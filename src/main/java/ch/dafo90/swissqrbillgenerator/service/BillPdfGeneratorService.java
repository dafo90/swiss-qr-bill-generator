package ch.dafo90.swissqrbillgenerator.service;

import ch.dafo90.swissqrbillgenerator.mapper.BillDocumentMapper;
import ch.dafo90.swissqrbillgenerator.model.BillData;
import ch.dafo90.swissqrbillgenerator.model.Document;
import ch.dafo90.swissqrbillgenerator.model.FieldMap;
import ch.dafo90.swissqrbillgenerator.model.PdfBill;
import ch.dafo90.swissqrbillgenerator.util.HtmlToPdfBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class BillPdfGeneratorService {

    private final BillDocumentMapper billDocumentMapper;
    private final TemplateEngine pdfTemplateEngine;

    public byte[] generateZippedBills(BillData billData) {
        return prepareZip(
                billData.getCsv()
                        .stream()
                        .map(row -> generateQrBillPdf(row, billData.getLogoBase64(), billData.getFieldsMap()))
                        .toList()
        );
    }

    private PdfBill generateQrBillPdf(Map<String, String> row, String logoBase64, Map<String, FieldMap> fieldsMap) {
        Document document = billDocumentMapper.toDocument(row, fieldsMap);
        log.info("Generate bill for {}", document.getRecipientName());
        byte[] pdfDocument = HtmlToPdfBuilder.convertToPdf(loadBillTemplate(logoBase64, document));
        return billDocumentMapper.toPdfBill(pdfDocument, row, fieldsMap);
    }

    private String loadBillTemplate(String logoBase64, Document document) {
        Context ctx = new Context();
        ctx.setVariable("document", document);
        ctx.setVariable("logoBase64", logoBase64);
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
