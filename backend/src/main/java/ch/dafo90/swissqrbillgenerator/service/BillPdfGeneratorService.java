package ch.dafo90.swissqrbillgenerator.service;

import ch.dafo90.swissqrbillgenerator.mapper.BillDocumentMapper;
import ch.dafo90.swissqrbillgenerator.model.BillData;
import ch.dafo90.swissqrbillgenerator.model.BillsData;
import ch.dafo90.swissqrbillgenerator.model.Document;
import ch.dafo90.swissqrbillgenerator.model.PdfBill;
import ch.dafo90.swissqrbillgenerator.model.csv.HeaderMap;
import ch.dafo90.swissqrbillgenerator.model.csv.Row;
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

    public byte[] generateZippedBills(BillsData billsData) {
        return prepareZip(
                billsData.getData()
                        .stream()
                        .map(row -> generateQrBillPdf(row, billsData.getLogoBase64(), billsData.getHeadersMap()))
                        .toList()
        );
    }

    public byte[] generateJpgPreview(BillData billData) {
        String logoBase64 = billData.getLogoBase64();
        Document document = billDocumentMapper.toDocument(billData.getData(), billData.getHeadersMap());
        log.info("Generate preview bill for {}", document.getRecipientName());
        return PdfToJpgBuilder.convertToJpg(HtmlToPdfBuilder.convertToPdf(loadBillTemplate(logoBase64, document)));
    }

    private PdfBill generateQrBillPdf(Row row, String logoBase64, List<HeaderMap> headersMap) {
        Document document = billDocumentMapper.toDocument(row, headersMap);
        log.info("Generate bill for {}", document.getRecipientName());
        byte[] pdfDocument = HtmlToPdfBuilder.convertToPdf(loadBillTemplate(logoBase64, document));
        return billDocumentMapper.toPdfBill(pdfDocument, row, headersMap);
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
