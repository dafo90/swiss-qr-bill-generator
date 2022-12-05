package ch.dafo90.swissqrbillgenerator.util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class PdfToJpgBuilder {

    public static byte[] convertToJpg(byte[] pdf) {
        try {
            PDDocument pdfDocument = PDDocument.load(pdf);
            PDFRenderer pdfRenderer = new PDFRenderer(pdfDocument);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            for (int page = 0; page < pdfDocument.getNumberOfPages(); ++page) {
                BufferedImage bim = pdfRenderer.renderImageWithDPI(page, 300, ImageType.RGB);
                ImageIO.write(bim, "jpg", baos);
            }
            byte[] imagePreview = baos.toByteArray();
            baos.close();
            pdfDocument.close();
            return imagePreview;
        } catch (IOException ex) {
            throw new RuntimeException("Cannot convert PDF to JPG", ex);
        }
    }

}
