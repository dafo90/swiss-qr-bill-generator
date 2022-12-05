package ch.dafo90.swissqrbillgenerator.util;

import ch.dafo90.swissqrbillgenerator.exception.ValidationException;
import ch.dafo90.swissqrbillgenerator.model.FieldType;
import ch.dafo90.swissqrbillgenerator.property.FieldProperties;
import com.github.javafaker.Faker;
import net.codecrete.qrbill.generator.Language;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Locale;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ValidatorUtilsTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"Text", "Text with spaces and special * characters! éèüö{}[]()/%&°+"})
    void validate_validRequiredFalse(String value) {
        FieldProperties fieldProperties = new FieldProperties(null, null, null, false, null, FieldType.TEXT, null, null, null);
        assertTrue(ValidatorUtils.validate("validate", value, fieldProperties));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Text", "Text with spaces and special * characters! éèüö{}[]()/%&°+"})
    void validate_validRequiredTrue(String value) {
        FieldProperties fieldProperties = new FieldProperties(null, null, null, true, null, FieldType.TEXT, null, null, null);
        assertTrue(ValidatorUtils.validate("validate", value, fieldProperties));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void validate_invalidRequiredTrue(String value) {
        FieldProperties fieldProperties = new FieldProperties(null, null, null, true, null, FieldType.TEXT, null, null, null);
        ValidationException ex = assertThrows(ValidationException.class, () -> ValidatorUtils.validate("validate", value, fieldProperties));
        assertEquals(String.format("Field 'validate' is required", value), ex.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Text", "Text with spaces and special * characters! éèüö{}[]()/%&°+"})
    void validateString_validWithoutOptions(String value) {
        assertTrue(ValidatorUtils.validateString("validateString", value, null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"VALUE1", "VALUE2"})
    void validateString_validWithOptions(String value) {
        List<String> options = List.of("VALUE1", "VALUE2");
        assertTrue(ValidatorUtils.validateString("validateString", value, options));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Text", "Text with spaces and special * characters! éèüö{}[]()/%&°+"})
    void validateString_invalidWithOptions(String value) {
        List<String> options = List.of("VALUE1", "VALUE2");
        ValidationException ex = assertThrows(ValidationException.class, () -> ValidatorUtils.validateString("validateString", value, options));
        assertEquals(String.format("Field 'validateString' has an unsupported text: VALUE1, VALUE2 only are supported (rejected value: '%s')", value), ex.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Text", "www.google.com", "http://google.com", "https://google.com"})
    void validateUrl_valid(String value) {
        assertTrue(ValidatorUtils.validateUrl("validateUrl", value));
    }

    @ParameterizedTest
    @ValueSource(strings = {"use-brackets{}", "use-special-characters%", "with spaces"})
    void validateUrl_invalid(String value) {
        ValidationException ex = assertThrows(ValidationException.class, () -> ValidatorUtils.validateUrl("validateUrl", value));
        assertEquals(String.format("Field 'validateUrl' is an invalid URL (rejected value: '%s')", value), ex.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"CH2863909WN7LXD1K67BO", "LI5271026VUFTR9HPFQPL"})
    void validateIban_valid(String value) {
        assertTrue(ValidatorUtils.validateIban("validateIban", value));
    }

    @Test
    void validateIban_invalid() {
        ValidationException ex = assertThrows(ValidationException.class, () -> ValidatorUtils.validateIban("validateIban", "RO63FNIQYL9PFRGJVTEHB3BR"));
        assertEquals("Field 'validateIban' is an invalid IBAN country, only CH and LI are accepted (rejected value: 'RO63FNIQYL9PFRGJVTEHB3BR')", ex.getMessage());

        ex = assertThrows(ValidationException.class, () -> ValidatorUtils.validateIban("validateIban", "CH28"));
        assertEquals("Field 'validateIban' has an invalid IBAN format (rejected value: 'CH28')", ex.getMessage());

        ex = assertThrows(ValidationException.class, () -> ValidatorUtils.validateIban("validateIban", "CH2863909WN7LXD1K67B1"));
        assertEquals("Field 'validateIban' is an invalid IBAN (rejected value: 'CH2863909WN7LXD1K67B1')", ex.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "10", "-15", "10.01", "-15.01"})
    void validateNumber_validWithoutOptions(String value) {
        assertTrue(ValidatorUtils.validateNumber("validateNumber", value, null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"1", "2"})
    void validateNumber_validWithOptions(String value) {
        List<String> options = List.of("1", "2");
        assertTrue(ValidatorUtils.validateNumber("validateNumber", value, options));
    }

    @ParameterizedTest
    @ValueSource(strings = {"10.", ".10", "10-", "1-0", "1-0.", "-.10", "10.-", ".-10", "1.-0", "1-.0"})
    void validateNumber_invalidWithoutOptions(String value) {
        ValidationException ex = assertThrows(ValidationException.class, () -> ValidatorUtils.validateNumber("validateNumber", value, null));
        assertEquals(String.format("Field 'validateNumber' is an invalid number (rejected value: '%s')", value), ex.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "3"})
    void validateNumber_invalidWithOptions(String value) {
        List<String> options = List.of("1", "2");
        ValidationException ex = assertThrows(ValidationException.class, () -> ValidatorUtils.validateNumber("validateNumber", value, options));
        assertEquals(String.format("Field 'validateNumber' has an unsupported value: 1, 2 only are supported (rejected value: '%s')", value), ex.getMessage());
    }

    private static Stream<String> getEmails() {
        return IntStream.rangeClosed(0, 20).mapToObj((i) -> new Faker().internet().emailAddress());
    }

    @ParameterizedTest
    @MethodSource("getEmails")
    void validateEmail_valid(String value) {
        assertTrue(ValidatorUtils.validateEmail("validateEmail", value));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Text", "invalid.email.address", "@invalid.email", "invalid.email@", "invalid@email@address"})
    void validateEmail_invalid(String value) {
        ValidationException ex = assertThrows(ValidationException.class, () -> ValidatorUtils.validateEmail("validateEmail", value));
        assertEquals(String.format("Field 'validateEmail' is an invalid email (rejected value: '%s')", value), ex.getMessage());
    }

    private static Stream<String> getAllCountries_formatPart1Alpha2() {
        return Locale.getISOCountries(Locale.IsoCountryCode.PART1_ALPHA2).stream();
    }

    @ParameterizedTest
    @MethodSource("getAllCountries_formatPart1Alpha2")
    void validateCountryCode_validWithoutOptions(String value) {
        assertTrue(ValidatorUtils.validateCountryCode("validateCountryCode", value, null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"CH", "DE"})
    void validateCountryCode_validWithOptions(String value) {
        List<String> options = List.of("CH", "DE");
        assertTrue(ValidatorUtils.validateCountryCode("validateCountryCode", value, options));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Fake country code", "AA"})
    void validateCountryCode_invalidWithoutOptions(String value) {
        ValidationException ex = assertThrows(ValidationException.class, () -> ValidatorUtils.validateCountryCode("validateCountryCode", value, null));
        assertEquals(String.format("Field 'validateCountryCode' is an invalid country code, countries in ISO 3166-1 alpha-2 only are accepted (rejected value: '%s')", value), ex.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"IT", "FR"})
    void validateCountryCode_invalidWithOptions(String value) {
        List<String> options = List.of("CH", "DE");
        ValidationException ex = assertThrows(ValidationException.class, () -> ValidatorUtils.validateCountryCode("validateCountryCode", value, options));
        assertEquals(String.format("Field 'validateCountryCode' is an unsupported country code: CH, DE only are supported (rejected value: %s)", value), ex.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"CHF", "EUR"})
    void validateCurrencyCode_validWithoutOptions(String value) {
        assertTrue(ValidatorUtils.validateCurrencyCode("validateCurrencyCode", value, null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"CHF", "USD"})
    void validateCurrencyCode_validWithOptions(String value) {
        List<String> options = List.of("CHF", "USD");
        assertTrue(ValidatorUtils.validateCurrencyCode("validateCurrencyCode", value, options));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Fake currency code", "USD", "GBP"})
    void validateCurrencyCode_invalidWithoutOptions(String value) {
        ValidationException ex = assertThrows(ValidationException.class, () -> ValidatorUtils.validateCurrencyCode("validateCurrencyCode", value, null));
        assertEquals(String.format("Field 'validateCurrencyCode' is an unsupported currency code: CHF, EUR only are supported (rejected value: '%s')", value), ex.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"EUR", "GBP"})
    void validateCurrencyCode_invalidWithOptions(String value) {
        List<String> options = List.of("CHF", "USD");
        ValidationException ex = assertThrows(ValidationException.class, () -> ValidatorUtils.validateCurrencyCode("validateCurrencyCode", value, options));
        assertEquals(String.format("Field 'validateCurrencyCode' is an unsupported currency code: CHF, USD only are supported (rejected value: '%s')", value), ex.getMessage());
    }

    private static Stream<String> getLanguages() {
        return Stream.of(Language.values()).map(Language::name);
    }

    @ParameterizedTest
    @MethodSource("getLanguages")
    void validateLanguageCode_validWithoutOptions(String value) {
        assertTrue(ValidatorUtils.validateLanguageCode("validateLanguageCode", value, null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"IT", "EN"})
    void validateLanguageCode_validWithOptions(String value) {
        List<String> options = List.of("IT", "EN");
        assertTrue(ValidatorUtils.validateLanguageCode("validateLanguageCode", value, options));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Fake language code", "HA", "IO"})
    void validateLanguageCode_invalidWithoutOptions(String value) {
        ValidationException ex = assertThrows(ValidationException.class, () -> ValidatorUtils.validateLanguageCode("validateLanguageCode", value, null));
        assertEquals(String.format("Field 'validateLanguageCode' is an unsupported language code: DE, FR, IT, RM, EN only are supported (rejected value: '%s')", value), ex.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"DE", "RM"})
    void validateLanguageCode_invalidWithOptions(String value) {
        List<String> options = List.of("IT", "EN");
        ValidationException ex = assertThrows(ValidationException.class, () -> ValidatorUtils.validateLanguageCode("validateLanguageCode", value, options));
        assertEquals(String.format("Field 'validateLanguageCode' is an unsupported language code: IT, EN only are supported (rejected value: '%s')", value), ex.getMessage());
    }
}
