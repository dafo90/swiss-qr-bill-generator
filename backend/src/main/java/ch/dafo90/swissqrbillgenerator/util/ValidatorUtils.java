package ch.dafo90.swissqrbillgenerator.util;

import ch.dafo90.swissqrbillgenerator.exception.ValidationException;
import ch.dafo90.swissqrbillgenerator.property.FieldProperties;
import net.codecrete.qrbill.generator.Language;
import org.iban4j.*;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class ValidatorUtils {

    public static boolean validate(String fieldName, String value, FieldProperties fieldProperties) {
        if (!StringUtils.hasText(value)) {
            if (fieldProperties.required()) {
                throw new ValidationException(String.format("Field '%s' is required", fieldName));
            } else {
                return true;
            }
        }
        return switch (fieldProperties.type()) {
            case STRING -> validateString(fieldName, value, fieldProperties.options());
            case TEXT, REFERENCE -> true; // Reference cannot be validated
            case URL -> validateUrl(fieldName, value);
            case IBAN -> validateIban(fieldName, value);
            case NUMBER -> validateNumber(fieldName, value, fieldProperties.options());
            case EMAIL -> validateEmail(fieldName, value);
            case COUNTRY_CODE -> validateCountryCode(fieldName, value, fieldProperties.options());
            case CURRENCY_CODE -> validateCurrencyCode(fieldName, value, fieldProperties.options());
            case LANGUAGE_CODE -> validateLanguageCode(fieldName, value, fieldProperties.options());
        };
    }

    protected static boolean validateString(String fieldName, String value, List<String> options) {
        if (!CollectionUtils.isEmpty(options) && !options.contains(value)) {
            throw new ValidationException(
                    String.format(
                            "Field '%s' has an unsupported text: %s only are supported (rejected value: '%s')",
                            fieldName,
                            StringUtils.collectionToDelimitedString(options, ", "),
                            value
                    ));
        }
        return true;
    }

    protected static boolean validateUrl(String fieldName, String value) {
        String urlToValidate = startWithProtocol(value) ? value : String.format("http://%s", value);
        try {
            new URL(urlToValidate).toURI();
        } catch (MalformedURLException | URISyntaxException ex) {
            throw new ValidationException(String.format("Field '%s' is an invalid URL (rejected value: '%s')", fieldName, value), ex);
        }
        return true;
    }

    private static boolean startWithProtocol(String value) {
        String lowerCaseValue = value.toLowerCase();
        return Stream.of("http://", "https://").anyMatch(lowerCaseValue::startsWith);
    }

    protected static boolean validateIban(String fieldName, String value) {
        if (!value.startsWith(CountryCode.CH.getAlpha2()) && !value.startsWith(CountryCode.LI.getAlpha2())) {
            throw new ValidationException(String.format("Field '%s' is an invalid IBAN country, only CH and LI are accepted (rejected value: '%s')", fieldName, value));
        }
        try {
            IbanUtil.validate(value);
        } catch (IbanFormatException ex) {
            throw new ValidationException(String.format("Field '%s' has an invalid IBAN format (rejected value: '%s')", fieldName, value), ex);
        } catch (InvalidCheckDigitException ex) {
            throw new ValidationException(String.format("Field '%s' is an invalid IBAN (rejected value: '%s')", fieldName, value), ex);
        } catch (UnsupportedCountryException ex) {
            throw new ValidationException(String.format("Field '%s' has an unsupported IBAN country (rejected value: '%s')", fieldName, value), ex);
        }
        return true;
    }

    protected static boolean validateNumber(String fieldName, String value, List<String> options) {
        if (CollectionUtils.isEmpty(options)) {
            if (!validateByRegex("-?\\d+(\\.\\d+)?", value)) {
                throw new ValidationException(String.format("Field '%s' is an invalid number (rejected value: '%s')", fieldName, value));
            }
        } else {
            if (!options.contains(value)) {
                throw new ValidationException(
                        String.format(
                                "Field '%s' has an unsupported value: %s only are supported (rejected value: '%s')",
                                fieldName,
                                StringUtils.collectionToDelimitedString(options, ", "),
                                value
                        ));
            }
        }
        return true;
    }

    protected static boolean validateEmail(String fieldName, String value) {
        if (!validateByRegex("^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$", value)) {
            throw new ValidationException(String.format("Field '%s' is an invalid email (rejected value: '%s')", fieldName, value));
        }
        return true;
    }

    protected static boolean validateCountryCode(String fieldName, String value, List<String> options) {
        List<String> countryCodes = CollectionUtils.isEmpty(options) ? Locale.getISOCountries(Locale.IsoCountryCode.PART1_ALPHA2).stream().toList() : options;
        if (!countryCodes.contains(value)) {
            if (CollectionUtils.isEmpty(options)) {
                throw new ValidationException(String.format("Field '%s' is an invalid country code, countries in ISO 3166-1 alpha-2 only are accepted (rejected value: '%s')", fieldName, value));
            } else {
                throw new ValidationException(
                        String.format(
                                "Field '%s' is an unsupported country code: %s only are supported (rejected value: %s)",
                                fieldName,
                                StringUtils.collectionToDelimitedString(countryCodes, ", "),
                                value
                        ));
            }
        }
        return true;
    }

    protected static boolean validateCurrencyCode(String fieldName, String value, List<String> options) {
        List<String> currencyCodes = CollectionUtils.isEmpty(options) ? List.of("CHF", "EUR") : options;
        if (!currencyCodes.contains(value)) {
            throw new ValidationException(
                    String.format(
                            "Field '%s' is an unsupported currency code: %s only are supported (rejected value: '%s')",
                            fieldName,
                            StringUtils.collectionToDelimitedString(currencyCodes, ", "),
                            value
                    ));
        }
        return true;
    }

    protected static boolean validateLanguageCode(String fieldName, String value, List<String> options) {
        List<String> languageCodes = CollectionUtils.isEmpty(options) ? Stream.of(Language.values()).map(Language::name).toList() : options;
        if (!languageCodes.contains(value)) {
            throw new ValidationException(
                    String.format(
                            "Field '%s' is an unsupported language code: %s only are supported (rejected value: '%s')",
                            fieldName,
                            StringUtils.collectionToDelimitedString(languageCodes, ", "),
                            value
                    ));
        }
        return true;
    }

    private static boolean validateByRegex(String regex, String value) {
        return Pattern.compile(regex).matcher(value).matches();
    }


}
