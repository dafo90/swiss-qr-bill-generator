package ch.dafo90.swissqrbillgenerator.configuration;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class RequestLoggingFilterConfigTest {

    private final RequestLoggingFilterConfig requestLoggingFilterConfig = new RequestLoggingFilterConfig(null);

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "    "})
    void sanitizePath_emptyPath(String path) {
        assertNull(requestLoggingFilterConfig.sanitizePath(path));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/", "//", "///"})
    void sanitizePath_slashPath(String path) {
        assertEquals("/", requestLoggingFilterConfig.sanitizePath(path));
    }

    @ParameterizedTest
    @ValueSource(strings = {"test", "/test", "test/", "/test/", "//test", "test//", "//test//"})
    void sanitizePath_oneDepthPath(String path) {
        assertEquals("/test/", requestLoggingFilterConfig.sanitizePath(path));
    }

    @ParameterizedTest
    @ValueSource(strings = {"test1/test2", "/test1/test2", "test1/test2/", "/test1/test2/", "//test1//test2", "test1//test2//", "//test1//test2//"})
    void sanitizePath_multiDepthPath(String path) {
        assertEquals("/test1/test2/", requestLoggingFilterConfig.sanitizePath(path));
    }

    @Nested
    class CustomRequestLoggingFilterTest {

        private final RequestLoggingFilterConfig.CustomRequestLoggingFilter applicationRequestLoggingFilter = new RequestLoggingFilterConfig.CustomRequestLoggingFilter();

        private static Stream<Stream<String>> emptyStream() {
            return Stream.of(
                    Stream.of(),
                    Stream.of(null, ""),
                    Stream.of("/")
            );
        }

        @ParameterizedTest
        @MethodSource("emptyStream")
        void filterEmptyString_emptyStream(Stream<String> exclusions) {
            assertEquals(Set.of(), applicationRequestLoggingFilter.filterEmptyString(exclusions));
        }

        private static Stream<Stream<String>> oneElementStream() {
            return Stream.of(
                    Stream.of("test"),
                    Stream.of(null, "", "test"),
                    Stream.of("/", "test")
            );
        }

        @ParameterizedTest
        @MethodSource("oneElementStream")
        void filterEmptyString_oneElementStream(Stream<String> exclusions) {
            assertEquals(Set.of("test"), applicationRequestLoggingFilter.filterEmptyString(exclusions));
        }

    }
}
