package ch.dafo90.swissqrbillgenerator.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MediaTypeUtilsTest {

    @ParameterizedTest
    @ValueSource(strings = {"image/jpeg", "image/jpg"})
    void check_validJpg(String actualMediaType) {
        assertTrue(MediaTypeUtils.check("image/jpeg", actualMediaType));
        assertTrue(MediaTypeUtils.check("image/jpg", actualMediaType));
    }

    @ParameterizedTest
    @ValueSource(strings = {"image/png", "text/csv"})
    void check_invalidJpg(String actualMediaType) {
        assertFalse(MediaTypeUtils.check("image/jpeg", actualMediaType));
        assertFalse(MediaTypeUtils.check("image/jpg", actualMediaType));
    }

    @Test
    void check_valid() {
        assertTrue(MediaTypeUtils.check("application/json", "application/json"));
    }

    @Test
    void check_invalid() {
        assertFalse(MediaTypeUtils.check("application/json", "text/csv"));
    }
}
