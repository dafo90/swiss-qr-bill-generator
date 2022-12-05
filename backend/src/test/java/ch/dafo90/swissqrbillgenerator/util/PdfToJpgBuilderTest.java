package ch.dafo90.swissqrbillgenerator.util;

import ch.dafo90.swissqrbillgenerator.ByteChecker;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PdfToJpgBuilderTest {

    @Test
    void convertToJpg() {
        byte[] pdf = HtmlToPdfBuilder.convertToPdf(HtmlToPdfBuilderTest.SIMPLE_HTML);
        byte[] jpg = PdfToJpgBuilder.convertToJpg(pdf);
        assertTrue(ByteChecker.isValidJpg(jpg));
    }


}
