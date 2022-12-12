package ch.dafo90.swissqrbillgenerator.service;

import ch.dafo90.swissqrbillgenerator.BaseTest;
import ch.dafo90.swissqrbillgenerator.model.FieldType;
import ch.dafo90.swissqrbillgenerator.model.validation.ValidationMessage;
import ch.dafo90.swissqrbillgenerator.property.FieldProperties;
import com.github.javafaker.Faker;
import net.codecrete.qrbill.generator.Language;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;
import java.util.Locale;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class ValidatorServiceTest extends BaseTest {

    @Mock
    private FieldGroupService fieldGroupServiceMock;

    @InjectMocks
    private ValidationService validationService;

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"Text", "Text with spaces and special * characters! éèüö{}[]()/%&°+"})
    void validate_validRequiredFalse(String value) {
        String fieldName = "validate";
        FieldProperties fieldProperties = new FieldProperties(null, null, null, false, null, FieldType.TEXT, null, null, null);
        when(fieldGroupServiceMock.getFieldPropertiesByName(eq(fieldName))).thenReturn(fieldProperties);
        when(fieldGroupServiceMock.getField(eq(fieldName), any(), any())).thenReturn(value);
        assertEquals(0, validationService.validate(fieldName, null, null).count());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Text", "Text with spaces and special * characters! éèüö{}[]()/%&°+"})
    void validate_validRequiredTrue(String value) {
        String fieldName = "validate";
        FieldProperties fieldProperties = new FieldProperties(null, null, null, true, null, FieldType.TEXT, null, null, null);
        when(fieldGroupServiceMock.getFieldPropertiesByName(eq(fieldName))).thenReturn(fieldProperties);
        when(fieldGroupServiceMock.getField(eq(fieldName), any(), any())).thenReturn(value);
        assertEquals(0, validationService.validate(fieldName, null, null).count());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void validate_invalidRequiredTrue(String value) {
        String fieldName = "validate";
        FieldProperties fieldProperties = new FieldProperties(null, null, null, true, null, FieldType.TEXT, null, null, null);
        when(fieldGroupServiceMock.getFieldPropertiesByName(eq(fieldName))).thenReturn(fieldProperties);
        when(fieldGroupServiceMock.getField(eq(fieldName), any(), any())).thenReturn(value);
        assertEquals(new ValidationMessage(fieldName, value, "Field 'validate' is required", ValidationMessage.REQUIRED), validationService.validate(fieldName, null, null).findFirst().get());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Text", "Text with spaces and special * characters! éèüö{}[]()/%&°+"})
    void validateString_validWithoutOptions(String value) {
        assertEquals(0, validationService.validateString("validateString", value, null).count());
    }

    @ParameterizedTest
    @ValueSource(strings = {"VALUE1", "VALUE2"})
    void validateString_validWithOptions(String value) {
        List<String> options = List.of("VALUE1", "VALUE2");
        assertEquals(0, validationService.validateString("validateString", value, options).count());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Text", "Text with spaces and special * characters! éèüö{}[]()/%&°+"})
    void validateString_invalidWithOptions(String value) {
        String fieldName = "validateString";
        List<String> options = List.of("VALUE1", "VALUE2");
        assertEquals(new ValidationMessage(
                        fieldName,
                        value,
                        String.format("Field 'validateString' has an unsupported text: VALUE1, VALUE2 only are supported (rejected value: '%s')", value),
                        ValidationMessage.UNSUPPORTED_STRING,
                        options.toArray(new String[0])).toString(),
                validationService.validateString(fieldName, value, options).findFirst().get().toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Text", "www.google.com", "http://google.com", "https://google.com"})
    void validateUrl_valid(String value) {
        assertEquals(0, validationService.validateUrl("validateUrl", value).count());
    }

    @ParameterizedTest
    @ValueSource(strings = {"use-brackets{}", "use-special-characters%", "with spaces"})
    void validateUrl_invalid(String value) {
        String fieldName = "validateUrl";
        assertEquals(new ValidationMessage(
                        fieldName,
                        value,
                        String.format("Field 'validateUrl' is an invalid URL (rejected value: '%s')", value),
                        ValidationMessage.INVALID_URL),
                validationService.validateUrl(fieldName, value).findFirst().get());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "10", "-15", "10.01", "-15.01"})
    void validateNumber_validWithoutOptions(String value) {
        assertEquals(0, validationService.validateNumber("validateNumber", value, null).count());
    }

    @ParameterizedTest
    @ValueSource(strings = {"1", "2"})
    void validateNumber_validWithOptions(String value) {
        List<String> options = List.of("1", "2");
        assertEquals(0, validationService.validateNumber("validateNumber", value, options).count());
    }

    @ParameterizedTest
    @ValueSource(strings = {"10.", ".10", "10-", "1-0", "1-0.", "-.10", "10.-", ".-10", "1.-0", "1-.0"})
    void validateNumber_invalidWithoutOptions(String value) {
        String fieldName = "validateNumber";
        assertEquals(new ValidationMessage(
                        fieldName,
                        value,
                        String.format("Field 'validateNumber' is an invalid number (rejected value: '%s')", value),
                        ValidationMessage.INVALID_NUMBER),
                validationService.validateNumber(fieldName, value, null).findFirst().get());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "3"})
    void validateNumber_invalidWithOptions(String value) {
        String fieldName = "validateNumber";
        List<String> options = List.of("1", "2");
        assertEquals(new ValidationMessage(
                        fieldName,
                        value,
                        String.format("Field 'validateNumber' has an unsupported value: 1, 2 only are supported (rejected value: '%s')", value),
                        ValidationMessage.UNSUPPORTED_NUMBER,
                        options.toArray(new String[0])).toString(),
                validationService.validateNumber(fieldName, value, options).findFirst().get().toString());
    }

    private static Stream<String> getEmails() {
        return IntStream.rangeClosed(0, 20).mapToObj((i) -> new Faker().internet().emailAddress());
    }

    @ParameterizedTest
    @MethodSource("getEmails")
    void validateEmail_valid(String value) {
        assertEquals(0, validationService.validateEmail("validateEmail", value).count());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Text", "invalid.email.address", "@invalid.email", "invalid.email@", "invalid@email@address"})
    void validateEmail_invalid(String value) {
        String fieldName = "validateEmail";
        assertEquals(new ValidationMessage(
                        fieldName,
                        value,
                        String.format("Field 'validateEmail' is an invalid email (rejected value: '%s')", value),
                        ValidationMessage.INVALID_EMAIL),
                validationService.validateEmail(fieldName, value).findFirst().get());
    }

    private static Stream<String> getAllCountries_formatPart1Alpha2() {
        return Locale.getISOCountries(Locale.IsoCountryCode.PART1_ALPHA2).stream();
    }

    @ParameterizedTest
    @MethodSource("getAllCountries_formatPart1Alpha2")
    void validateCountryCode_validWithoutOptions(String value) {
        assertEquals(0, validationService.validateCountryCode("validateCountryCode", value, null).count());
    }

    @ParameterizedTest
    @ValueSource(strings = {"CH", "DE"})
    void validateCountryCode_validWithOptions(String value) {
        List<String> options = List.of("CH", "DE");
        assertEquals(0, validationService.validateCountryCode("validateCountryCode", value, options).count());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Fake country code", "AA"})
    void validateCountryCode_invalidWithoutOptions(String value) {
        String fieldName = "validateCountryCode";
        assertEquals(new ValidationMessage(
                        fieldName,
                        value,
                        String.format("Field 'validateCountryCode' is an invalid country code, countries in ISO 3166-1 alpha-2 only are accepted (rejected value: '%s')", value),
                        ValidationMessage.INVALID_COUNTRY_CODE),
                validationService.validateCountryCode(fieldName, value, null).findFirst().get());
    }

    @ParameterizedTest
    @ValueSource(strings = {"IT", "FR"})
    void validateCountryCode_invalidWithOptions(String value) {
        String fieldName = "validateCountryCode";
        List<String> options = List.of("CH", "DE");
        assertEquals(new ValidationMessage(
                        fieldName,
                        value,
                        String.format("Field 'validateCountryCode' is an unsupported country code: CH, DE only are supported (rejected value: %s)", value),
                        ValidationMessage.UNSUPPORTED_COUNTRY_CODE,
                        options.toArray(new String[0])).toString(),
                validationService.validateCountryCode(fieldName, value, options).findFirst().get().toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"CHF", "EUR"})
    void validateCurrencyCode_validWithoutOptions(String value) {
        assertEquals(0, validationService.validateCurrencyCode("validateCurrencyCode", value, null).count());
    }

    @ParameterizedTest
    @ValueSource(strings = {"CHF", "USD"})
    void validateCurrencyCode_validWithOptions(String value) {
        List<String> options = List.of("CHF", "USD");
        assertEquals(0, validationService.validateCurrencyCode("validateCurrencyCode", value, options).count());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Fake currency code", "USD", "GBP"})
    void validateCurrencyCode_invalidWithoutOptions(String value) {
        String fieldName = "validateCurrencyCode";
        assertEquals(new ValidationMessage(
                        fieldName,
                        value,
                        String.format("Field 'validateCurrencyCode' is an unsupported currency code: CHF, EUR only are supported (rejected value: '%s')", value),
                        ValidationMessage.UNSUPPORTED_CURRENCY_CODE,
                        "CHF", "EUR").toString(),
                validationService.validateCurrencyCode(fieldName, value, null).findFirst().get().toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"EUR", "GBP"})
    void validateCurrencyCode_invalidWithOptions(String value) {
        String fieldName = "validateCurrencyCode";
        List<String> options = List.of("CHF", "USD");
        assertEquals(new ValidationMessage(
                        fieldName,
                        value,
                        String.format("Field 'validateCurrencyCode' is an unsupported currency code: CHF, USD only are supported (rejected value: '%s')", value),
                        ValidationMessage.UNSUPPORTED_CURRENCY_CODE,
                        options.toArray(new String[0])).toString(),
                validationService.validateCurrencyCode(fieldName, value, options).findFirst().get().toString());
    }

    private static Stream<String> getLanguages() {
        return Stream.of(Language.values()).map(Language::name);
    }

    @ParameterizedTest
    @MethodSource("getLanguages")
    void validateLanguageCode_validWithoutOptions(String value) {
        assertEquals(0, validationService.validateLanguageCode("validateLanguageCode", value, null).count());
    }

    @ParameterizedTest
    @ValueSource(strings = {"IT", "EN"})
    void validateLanguageCode_validWithOptions(String value) {
        List<String> options = List.of("IT", "EN");
        assertEquals(0, validationService.validateLanguageCode("validateLanguageCode", value, options).count());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Fake language code", "HA", "IO"})
    void validateLanguageCode_invalidWithoutOptions(String value) {
        String fieldName = "validateLanguageCode";
        assertEquals(new ValidationMessage(
                        fieldName,
                        value,
                        String.format("Field 'validateLanguageCode' is an unsupported language code: DE, FR, IT, RM, EN only are supported (rejected value: '%s')", value),
                        ValidationMessage.UNSUPPORTED_LANGUAGE_CODE,
                        "DE", "FR", "IT", "RM", "EN").toString(),
                validationService.validateLanguageCode(fieldName, value, null).findFirst().get().toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"DE", "RM"})
    void validateLanguageCode_invalidWithOptions(String value) {
        String fieldName = "validateLanguageCode";
        List<String> options = List.of("IT", "EN");
        assertEquals(new ValidationMessage(
                        fieldName,
                        value,
                        String.format("Field 'validateLanguageCode' is an unsupported language code: IT, EN only are supported (rejected value: '%s')", value),
                        ValidationMessage.UNSUPPORTED_LANGUAGE_CODE,
                        options.toArray(new String[0])).toString(),
                validationService.validateLanguageCode(fieldName, value, options).findFirst().get().toString());
    }
}
