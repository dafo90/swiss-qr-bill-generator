package ch.dafo90.swissqrbillgenerator.util;

import ch.dafo90.swissqrbillgenerator.ByteChecker;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class HtmlToPdfBuilderTest {

    public final static String SIMPLE_HTML = """
                <!DOCTYPE html>
                <html>
                <body>
                
                <h1>My First Heading</h1>
                <p>My first paragraph.</p>
                
                </body>
                </html>
            """;

    @Test
    void convertToPdf() {
        byte[] pdf = HtmlToPdfBuilder.convertToPdf(SIMPLE_HTML);
        assertTrue(ByteChecker.isValidPdf(pdf));
    }
}
