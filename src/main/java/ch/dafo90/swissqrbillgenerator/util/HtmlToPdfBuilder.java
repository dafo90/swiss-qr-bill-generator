package ch.dafo90.swissqrbillgenerator.util;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.openhtmltopdf.svgsupport.BatikSVGDrawer;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
public class HtmlToPdfBuilder {

    public static byte[] convertToPdf(String html) {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withW3cDocument(parseHtml5(html), "");
            builder.useSVGDrawer(new BatikSVGDrawer());
            builder.toStream(os);
            builder.run();
            return os.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Cannot convert HTML to PDF", e);
        }
    }

    private static org.w3c.dom.Document parseHtml5(String html) {
        return new W3CDom().fromJsoup(Jsoup.parse(html, StandardCharsets.UTF_8.name()));
    }

}


