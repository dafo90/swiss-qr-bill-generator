package ch.dafo90.swissqrbillgenerator;

import org.apache.tika.Tika;

import java.util.List;

public class ByteChecker {

    private static final Tika TIKA = new Tika();

    public static boolean isValidPdf(byte[] bytes) {
        return bytes == null ? false : "application/pdf".equals(TIKA.detect(bytes));
    }

    public static boolean isValidJpg(byte[] bytes) {
        return bytes == null ? false : List.of("image/jpg", "image/jpeg").contains(TIKA.detect(bytes));
    }

}
