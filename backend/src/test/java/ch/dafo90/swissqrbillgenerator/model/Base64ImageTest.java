package ch.dafo90.swissqrbillgenerator.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class Base64ImageTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "      "})
    void of_emptyImage(String imageBase64) {
        Base64Image image = Base64Image.of(imageBase64);
        assertTrue(image.isValid());
        assertTrue(image.isEmpty());
        assertNull(image.getDataUrl());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD/2wBDAAgGBgcGBQgHBwcJCQgKDBQNDAsLDBkSEw8UHRofHh0aHBwgJC4nICIsIxwcKDcpLDAxNDQ0Hyc5PTgyPC4zNDL/2wBDAQkJCQwLDBgNDRgyIRwhMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjL/wAARCAAFAAUDASIAAhEBAxEB/8QAHwAAAQUBAQEBAQEAAAAAAAAAAAECAwQFBgcICQoL/8QAtRAAAgEDAwIEAwUFBAQAAAF9AQIDAAQRBRIhMUEGE1FhByJxFDKBkaEII0KxwRVS0fAkM2JyggkKFhcYGRolJicoKSo0NTY3ODk6Q0RFRkdISUpTVFVWV1hZWmNkZWZnaGlqc3R1dnd4eXqDhIWGh4iJipKTlJWWl5iZmqKjpKWmp6ipqrKztLW2t7i5usLDxMXGx8jJytLT1NXW19jZ2uHi4+Tl5ufo6erx8vP09fb3+Pn6/8QAHwEAAwEBAQEBAQEBAQAAAAAAAAECAwQFBgcICQoL/8QAtREAAgECBAQDBAcFBAQAAQJ3AAECAxEEBSExBhJBUQdhcRMiMoEIFEKRobHBCSMzUvAVYnLRChYkNOEl8RcYGRomJygpKjU2Nzg5OkNERUZHSElKU1RVVldYWVpjZGVmZ2hpanN0dXZ3eHl6goOEhYaHiImKkpOUlZaXmJmaoqOkpaanqKmqsrO0tba3uLm6wsPExcbHyMnK0tPU1dbX2Nna4uPk5ebn6Onq8vP09fb3+Pn6/9oADAMBAAIRAxEAPwDpfDvh3Ub/AFjXLe38QXVpJa3GyWWMNmc7nG5sOOeCec9aKKK46FCEoXfn1fc9/McxxFPEOMWrWj9mP8qfY//Z",
            "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAUAAAAFCAYAAACNbyblAAAAHElEQVQI12P4//8/w38GIAXDIBKE0DHxgljNBAAO9TXL0Y4OHwAAAABJRU5ErkJggg=="
    })
    void of_validImage(String imageBase64) {
        Base64Image image = Base64Image.of(imageBase64);
        assertTrue(image.isValid());
        assertFalse(image.isEmpty());
        assertNotNull(image.getDataUrl());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            // Invalid Base64
            "data:image/jpeg;base64,InvalidBase64",

            // Invalid MediaType
            "data:image/jpeg;base64,iVBORw0KGgoAAAANSUhEUgAAAAUAAAAFCAYAAACNbyblAAAAHElEQVQI12P4//8/w38GIAXDIBKE0DHxgljNBAAO9TXL0Y4OHwAAAABJRU5ErkJggg==",

            // Invalid format
            "data:image/jpeg;base64",

            // Invalid format
            ":image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAUAAAAFCAYAAACNbyblAAAAHElEQVQI12P4//8/w38GIAXDIBKE0DHxgljNBAAO9TXL0Y4OHwAAAABJRU5ErkJggg=="
    })
    void of_invalidImage(String imageBase64) {
        Base64Image image = Base64Image.of(imageBase64);
        assertFalse(image.isValid());
        assertFalse(image.isEmpty());
        assertNull(image.getDataUrl());
    }

    @Test
    void getDataUrl() {
        String validImageBase64 = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAUAAAAFCAYAAACNbyblAAAAHElEQVQI12P4//8/w38GIAXDIBKE0DHxgljNBAAO9TXL0Y4OHwAAAABJRU5ErkJggg==";
        Base64Image image = Base64Image.of(validImageBase64);
        assertEquals("data:image/png;charset=US-ASCII,iVBORw0KGgoAAAANSUhEUgAAAAUAAAAFCAYAAACNbyblAAAAHElEQVQI12P4//8/w38GIAXDIBKE0DHxgljNBAAO9TXL0Y4OHwAAAABJRU5ErkJggg==", image.getDataUrl());
    }

    @Test
    void of_base64PlainText() {
        String validImageBase64 = "data:text/plain;base64,SGVsbG8gV29ybGQh";
        Base64Image image = Base64Image.of(validImageBase64);
        assertFalse(image.isValid());
        assertFalse(image.isEmpty());
        assertNull(image.getDataUrl());
    }

}
