package ch.dafo90.swissqrbillgenerator.util;

import java.util.List;
import java.util.stream.Stream;

public class MediaTypeUtils {

    public static boolean check(String expectedMediaType, String actualMediaType) {
        expectedMediaType = expectedMediaType.toLowerCase();
        actualMediaType = actualMediaType.toLowerCase();
        if (Stream.of("jpg", "jpeg").anyMatch(expectedMediaType::contains)) {
            return List.of("image/jpg", "image/jpeg").contains(actualMediaType);
        }
        return expectedMediaType.equals(actualMediaType);
    }

}
