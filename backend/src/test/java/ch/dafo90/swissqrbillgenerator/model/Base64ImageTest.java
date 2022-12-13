package ch.dafo90.swissqrbillgenerator.model;

import ch.dafo90.swissqrbillgenerator.exception.ImageException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class Base64ImageTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "      "})
    void of_validEmptyImage(String imageBase64) {
        Base64Image image = Base64Image.of(imageBase64);
        assertNull(image.getDataUrl());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD/2wBDAAgGBgcGBQgHBwcJCQgKDBQNDAsLDBkSEw8UHRofHh0aHBwgJC4nICIsIxwcKDcpLDAxNDQ0Hyc5PTgyPC4zNDL/2wBDAQkJCQwLDBgNDRgyIRwhMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjL/wAARCAAFAAUDASIAAhEBAxEB/8QAHwAAAQUBAQEBAQEAAAAAAAAAAAECAwQFBgcICQoL/8QAtRAAAgEDAwIEAwUFBAQAAAF9AQIDAAQRBRIhMUEGE1FhByJxFDKBkaEII0KxwRVS0fAkM2JyggkKFhcYGRolJicoKSo0NTY3ODk6Q0RFRkdISUpTVFVWV1hZWmNkZWZnaGlqc3R1dnd4eXqDhIWGh4iJipKTlJWWl5iZmqKjpKWmp6ipqrKztLW2t7i5usLDxMXGx8jJytLT1NXW19jZ2uHi4+Tl5ufo6erx8vP09fb3+Pn6/8QAHwEAAwEBAQEBAQEBAQAAAAAAAAECAwQFBgcICQoL/8QAtREAAgECBAQDBAcFBAQAAQJ3AAECAxEEBSExBhJBUQdhcRMiMoEIFEKRobHBCSMzUvAVYnLRChYkNOEl8RcYGRomJygpKjU2Nzg5OkNERUZHSElKU1RVVldYWVpjZGVmZ2hpanN0dXZ3eHl6goOEhYaHiImKkpOUlZaXmJmaoqOkpaanqKmqsrO0tba3uLm6wsPExcbHyMnK0tPU1dbX2Nna4uPk5ebn6Onq8vP09fb3+Pn6/9oADAMBAAIRAxEAPwDpfDvh3Ub/AFjXLe38QXVpJa3GyWWMNmc7nG5sOOeCec9aKKK46FCEoXfn1fc9/McxxFPEOMWrWj9mP8qfY//Z",
            "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAUAAAAFCAYAAACNbyblAAAAHElEQVQI12P4//8/w38GIAXDIBKE0DHxgljNBAAO9TXL0Y4OHwAAAABJRU5ErkJggg=="
    })
    void of_validImage(String imageBase64) {
        Base64Image image = Base64Image.of(imageBase64);
        assertNotNull(image.getDataUrl());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            // Invalid Base64
            "data:image/jpeg;base64,InvalidBase64",

            // Invalid MediaType
            "data:image/jpeg;base64,iVBORw0KGgoAAAANSUhEUgAAAAUAAAAFCAYAAACNbyblAAAAHElEQVQI12P4//8/w38GIAXDIBKE0DHxgljNBAAO9TXL0Y4OHwAAAABJRU5ErkJggg==",


    })
    @Test
    void of_invalidFormat() {
        ImageException ex = assertThrows(ImageException.class, () -> Base64Image.of(":image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAUAAAAFCAYAAACNbyblAAAAHElEQVQI12P4//8/w38GIAXDIBKE0DHxgljNBAAO9TXL0Y4OHwAAAABJRU5ErkJggg=="));
        assertTrue(ex.getMessage().startsWith("Invalid image: doesn't start with 'data:' (must be a string containing the requested data URL, see https://developer.mozilla.org/en-US/docs/Web/HTTP/Basics_of_HTTP/Data_URLs)"));
    }

    @Test
    void of_invalidCommaFormat() {
        ImageException ex = assertThrows(ImageException.class, () -> Base64Image.of("data:image/jpeg;iVBORw0KGgoAAAANSUhEUgAAAAUAAAAFCAYAAACNbyblAAAAHElEQVQI12P4//8/w38GIAXDIBKE0DHxgljNBAAO9TXL0Y4OHwAAAABJRU5ErkJggg=="));
        assertTrue(ex.getMessage().startsWith("Invalid image: found 1 comma separated token, expected 2 (must be a string containing the requested data URL, see https://developer.mozilla.org/en-US/docs/Web/HTTP/Basics_of_HTTP/Data_URLs)"));
    }

    @Test
    void of_invalidColonFormat() {
        ImageException ex = assertThrows(ImageException.class, () -> Base64Image.of("data:data1:image/jpeg,iVBORw0KGgoAAAANSUhEUgAAAAUAAAAFCAYAAACNbyblAAAAHElEQVQI12P4//8/w38GIAXDIBKE0DHxgljNBAAO9TXL0Y4OHwAAAABJRU5ErkJggg=="));
        assertTrue(ex.getMessage().startsWith("Invalid image data: found 3 colon separated token, expected 2 (must be a string containing the requested data URL, see https://developer.mozilla.org/en-US/docs/Web/HTTP/Basics_of_HTTP/Data_URLs)"));
    }

    @Test
    void of_invalidMediaTypeImage() {
        ImageException ex = assertThrows(ImageException.class, () -> Base64Image.of("data:application/json,iVBORw0KGgoAAAANSUhEUgAAAAUAAAAFCAYAAACNbyblAAAAHElEQVQI12P4//8/w38GIAXDIBKE0DHxgljNBAAO9TXL0Y4OHwAAAABJRU5ErkJggg=="));
        assertTrue(ex.getMessage().startsWith("Invalid image: unsupported media type 'application/json', 'image/*' only are supported"));
    }

    @Test
    void of_invalidMediaType() {
        ImageException ex = assertThrows(ImageException.class, () -> Base64Image.of("data:image/jpg,iVBORw0KGgoAAAANSUhEUgAAAAUAAAAFCAYAAACNbyblAAAAHElEQVQI12P4//8/w38GIAXDIBKE0DHxgljNBAAO9TXL0Y4OHwAAAABJRU5ErkJggg=="));
        assertTrue(ex.getMessage().startsWith("Invalid image: defined media type 'image/jpg', detected 'image/png'"));
    }

    @Test
    void getDataUrl() {
        String validImageBase64 = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAUAAAAFCAYAAACNbyblAAAAHElEQVQI12P4//8/w38GIAXDIBKE0DHxgljNBAAO9TXL0Y4OHwAAAABJRU5ErkJggg==";
        Base64Image image = Base64Image.of(validImageBase64);
        assertEquals("data:image/png;charset=US-ASCII,iVBORw0KGgoAAAANSUhEUgAAAAUAAAAFCAYAAACNbyblAAAAHElEQVQI12P4//8/w38GIAXDIBKE0DHxgljNBAAO9TXL0Y4OHwAAAABJRU5ErkJggg==", image.getDataUrl());
    }

}
