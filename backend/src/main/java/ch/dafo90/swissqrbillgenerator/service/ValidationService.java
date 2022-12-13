package ch.dafo90.swissqrbillgenerator.service;

import ch.dafo90.swissqrbillgenerator.exception.ImageException;
import ch.dafo90.swissqrbillgenerator.mapper.BillDocumentMapper;
import ch.dafo90.swissqrbillgenerator.model.Base64Image;
import ch.dafo90.swissqrbillgenerator.model.BillData;
import ch.dafo90.swissqrbillgenerator.model.BillsData;
import ch.dafo90.swissqrbillgenerator.model.csv.HeaderMap;
import ch.dafo90.swissqrbillgenerator.model.csv.Row;
import ch.dafo90.swissqrbillgenerator.model.validation.ValidationMessage;
import ch.dafo90.swissqrbillgenerator.model.validation.ValidationResult;
import ch.dafo90.swissqrbillgenerator.property.AppProperties;
import ch.dafo90.swissqrbillgenerator.property.FieldGroupProperties;
import ch.dafo90.swissqrbillgenerator.property.FieldProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.codecrete.qrbill.generator.Bill;
import net.codecrete.qrbill.generator.Language;
import net.codecrete.qrbill.generator.QRBill;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class ValidationService {

    private static final String LOGO_FIELD_NAME = "logoBase64";

    private static final Map<String, String> ERROR_FIELDS = Map.ofEntries(
            Map.entry("currency", FieldProperties.CURRENCY),
            Map.entry("amount", FieldProperties.AMOUNT),
            Map.entry("account", FieldProperties.ACCOUNT),
            Map.entry("reference", FieldProperties.REFERENCE),
            Map.entry("referenceType", FieldProperties.REFERENCE),
            Map.entry("unstructuredMessage", FieldProperties.UNSTRUCTURED_MESSAGE),

            Map.entry("creditor.name", FieldProperties.CREDITOR_NAME),
            Map.entry("creditor.addressLine1", FieldProperties.CREDITOR_STREET),
            Map.entry("creditor.addressLine2", FieldProperties.CREDITOR_LOCALITY),
            Map.entry("creditor.countryCode", FieldProperties.CREDITOR_COUNTRY),

            Map.entry("debtor.name", FieldProperties.DEBTOR_NAME),
            Map.entry("debtor.addressLine1", FieldProperties.DEBTOR_STREET),
            Map.entry("debtor.addressLine2", FieldProperties.DEBTOR_LOCALITY),
            Map.entry("debtor.countryCode", FieldProperties.DEBTOR_COUNTRY)
    );

    private static final Map<String, String> ERROR_MESSAGES = Map.ofEntries(
            Map.entry("currency_not_chf_or_eur", "Currency should be 'CHF' or 'EUR'"),
            Map.entry("amount_outside_valid_range", "Amount should be between 0.01 and 999 999 999.99"),
            Map.entry("account_iban_not_from_ch_or_li", "Account number should start with 'CH' or 'LI'"),
            Map.entry("account_iban_invalid", "Account number is not a valid IBAN (invalid format or checksum)"),
            Map.entry("ref_invalid", "Reference is invalid; it is neither a valid QR reference nor a valid ISO 11649 reference"),
            Map.entry("qr_ref_missing", "QR reference is missing; it is mandatory for payments to a QR-IBAN account"),
            Map.entry("cred_ref_invalid_use_for_qr_iban", "For payments to a QR-IBAN account, a QR reference is required (an ISO 11649 reference may not be used)"),
            Map.entry("qr_ref_invalid_use_for_non_qr_iban", "A QR reference is only allowed for payments to a QR-IBAN account"),
            Map.entry("ref_type_invalid", "Reference type should be one of 'QRR', 'SCOR' and 'NON' and match the reference"),
            Map.entry("field_value_missing", "Field '%s' may not be empty"),
            Map.entry("address_type_conflict", "Fields for either structured address or combined elements address may be filled but not both"),
            Map.entry("country_code_invalid", "Country code is invalid; it should consist of two letters"),
            Map.entry("field_value_clipped", "The value for field '%s' has been clipped to not exceed the maximum length of %s characters"),
            Map.entry("field_value_too_long", "The value for field '%s' should not exceed a length of %s characters"),
            Map.entry("additional_info_too_long", "The additional information and the structured bill information combined should not exceed 140 characters"),
            Map.entry("replaced_unsupported_characters", "Unsupported characters have been replaced in field '%s'"),
            Map.entry("alt_scheme_max_exceed", "No more than two alternative schemes may be used"),
            Map.entry("bill_info_invalid", "Structured bill information must start with '//'")
    );

    private final AppProperties appProperties;
    private final BillDocumentMapper billDocumentMapper;
    private final FieldGroupService fieldGroupService;

    public ValidationResult validate(BillData billData) {
        List<ValidationMessage> validationMessages = new ArrayList<>(validateSingleBill(billData.getData(), billData.getHeadersMap()).toList());
        Base64Image logo = Base64Image.empty();
        try {
            logo = Base64Image.of(billData.getLogoBase64());
        } catch (ImageException ex) {
            validationMessages.add(new ValidationMessage(LOGO_FIELD_NAME, null, ex.getMessage(), ex.getMessageKey()));
        }
        return new ValidationResult(logo, validationMessages);
    }

    public ValidationResult validate(BillsData billsData) {
        List<ValidationMessage> validationMessages = new ArrayList<>(billsData.getData().stream().flatMap(row -> validateSingleBill(row, billsData.getHeadersMap())).toList());
        Base64Image logo = Base64Image.empty();
        try {
            logo = Base64Image.of(billsData.getLogoBase64());
        } catch (ImageException ex) {
            validationMessages.add(new ValidationMessage(LOGO_FIELD_NAME, null, ex.getMessage(), ex.getMessageKey()));
        }
        return new ValidationResult(logo, validationMessages);
    }

    private Stream<ValidationMessage> validateSingleBill(Row row, List<HeaderMap> headersMap) {
        Bill bill = billDocumentMapper.toQrBill(row, headersMap);

        Map<String, ValidationMessage> qrBillValidationMessages = QRBill.validate(bill).getValidationMessages().stream()
                .map(validationMessage -> mapValidationMessage(validationMessage, row, headersMap))
                .collect(Collectors.toMap(ValidationMessage::getField, Function.identity()));

        Map<String, ValidationMessage> validationMessages = appProperties.groups().stream()
                .map(FieldGroupProperties::fields)
                .flatMap(List::stream)
                .map(FieldProperties::name)
                .flatMap(fieldName -> validate(fieldName, row, headersMap))
                .collect(Collectors.toMap(ValidationMessage::getField, Function.identity()));

        Map<String, ValidationMessage> allValidationMessages = new HashMap<>();
        allValidationMessages.putAll(qrBillValidationMessages);
        allValidationMessages.putAll(validationMessages);

        return allValidationMessages.values().stream();
    }

    private ValidationMessage mapValidationMessage(net.codecrete.qrbill.generator.ValidationMessage validationMessage, Row row, List<HeaderMap> headersMap) {
        String fieldName = ERROR_FIELDS.get(validationMessage.getField());
        String rejectedValue = fieldGroupService.getField(fieldName, row, headersMap);
        String message = String.format(ERROR_MESSAGES.get(validationMessage.getMessageKey()), validationMessage.getMessageParameters());

        return new ValidationMessage(fieldName, rejectedValue, message, validationMessage.getMessageKey(), validationMessage.getMessageParameters());
    }


    protected Stream<ValidationMessage> validate(String fieldName, Row row, List<HeaderMap> headersMap) {
        FieldProperties fieldProperties = fieldGroupService.getFieldPropertiesByName(fieldName);
        String value = fieldGroupService.getField(fieldName, row, headersMap);

        if (!StringUtils.hasText(value)) {
            if (fieldProperties.required()) {
                return Stream.of(new ValidationMessage(
                        fieldName,
                        value,
                        String.format("Field '%s' is required", fieldName),
                        ValidationMessage.REQUIRED));
            } else {
                return Stream.empty();
            }
        }
        return switch (fieldProperties.type()) {
            case STRING -> validateString(fieldName, value, fieldProperties.options());
            case URL -> validateUrl(fieldName, value);
            case NUMBER -> validateNumber(fieldName, value, fieldProperties.options());
            case EMAIL -> validateEmail(fieldName, value);
            case COUNTRY_CODE -> validateCountryCode(fieldName, value, fieldProperties.options());
            case CURRENCY_CODE -> validateCurrencyCode(fieldName, value, fieldProperties.options());
            case LANGUAGE_CODE -> validateLanguageCode(fieldName, value, fieldProperties.options());
            default -> Stream.empty();
        };
    }

    protected Stream<ValidationMessage> validateString(String fieldName, String value, List<String> options) {
        if (!CollectionUtils.isEmpty(options) && !options.contains(value)) {
            return Stream.of(new ValidationMessage(
                    fieldName,
                    value,
                    String.format(
                            "Field '%s' has an unsupported text: %s only are supported (rejected value: '%s')",
                            fieldName,
                            StringUtils.collectionToDelimitedString(options, ", "),
                            value
                    ),
                    ValidationMessage.UNSUPPORTED_STRING,
                    StringUtils.collectionToDelimitedString(options, ", ")
            ));
        }
        return Stream.empty();
    }

    protected Stream<ValidationMessage> validateUrl(String fieldName, String value) {
        String urlToValidate = startWithProtocol(value) ? value : String.format("http://%s", value);
        try {
            new URL(urlToValidate).toURI();
            return Stream.empty();
        } catch (MalformedURLException | URISyntaxException ex) {
            return Stream.of(new ValidationMessage(
                    fieldName,
                    value,
                    String.format("Field '%s' is an invalid URL (rejected value: '%s')", fieldName, value),
                    ValidationMessage.INVALID_URL));
        }
    }

    private boolean startWithProtocol(String value) {
        String lowerCaseValue = value.toLowerCase();
        return Stream.of("http://", "https://").anyMatch(lowerCaseValue::startsWith);
    }

    protected Stream<ValidationMessage> validateNumber(String fieldName, String value, List<String> options) {
        if (CollectionUtils.isEmpty(options)) {
            if (!validateByRegex("-?\\d+(\\.\\d+)?", value)) {
                return Stream.of(new ValidationMessage(
                        fieldName,
                        value,
                        String.format("Field '%s' is an invalid number (rejected value: '%s')", fieldName, value),
                        ValidationMessage.INVALID_NUMBER));
            }
        } else if (!options.contains(value)) {
            return Stream.of(new ValidationMessage(
                    fieldName,
                    value,
                    String.format(
                            "Field '%s' has an unsupported value: %s only are supported (rejected value: '%s')",
                            fieldName,
                            StringUtils.collectionToDelimitedString(options, ", "),
                            value
                    ),
                    ValidationMessage.UNSUPPORTED_NUMBER,
                    StringUtils.collectionToDelimitedString(options, ", ")));
        }
        return Stream.empty();
    }

    protected Stream<ValidationMessage> validateEmail(String fieldName, String value) {
        if (!validateByRegex("^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$", value)) {
            return Stream.of(new ValidationMessage(
                    fieldName,
                    value,
                    String.format("Field '%s' is an invalid email (rejected value: '%s')", fieldName, value),
                    ValidationMessage.INVALID_EMAIL));
        }
        return Stream.empty();
    }

    protected Stream<ValidationMessage> validateCountryCode(String fieldName, String value, List<String> options) {
        List<String> countryCodes = CollectionUtils.isEmpty(options) ? Locale.getISOCountries(Locale.IsoCountryCode.PART1_ALPHA2).stream().toList() : options;
        if (!countryCodes.contains(value)) {
            if (CollectionUtils.isEmpty(options)) {
                return Stream.of(new ValidationMessage(
                        fieldName,
                        value,
                        String.format("Field '%s' is an invalid country code, countries in ISO 3166-1 alpha-2 only are accepted (rejected value: '%s')", fieldName, value),
                        ValidationMessage.INVALID_COUNTRY_CODE));
            } else {
                return Stream.of(new ValidationMessage(
                        fieldName,
                        value,
                        String.format(
                                "Field '%s' is an unsupported country code: %s only are supported (rejected value: %s)",
                                fieldName,
                                StringUtils.collectionToDelimitedString(options, ", "),
                                value
                        ),
                        ValidationMessage.UNSUPPORTED_COUNTRY_CODE,
                        StringUtils.collectionToDelimitedString(options, ", ")));
            }
        }
        return Stream.empty();
    }

    protected Stream<ValidationMessage> validateCurrencyCode(String fieldName, String value, List<String> options) {
        List<String> currencyCodes = CollectionUtils.isEmpty(options) ? List.of("CHF", "EUR") : options;
        if (!currencyCodes.contains(value)) {
            return Stream.of(new ValidationMessage(
                    fieldName,
                    value,
                    String.format(
                            "Field '%s' is an unsupported currency code: %s only are supported (rejected value: '%s')",
                            fieldName,
                            StringUtils.collectionToDelimitedString(currencyCodes, ", "),
                            value
                    ),
                    ValidationMessage.UNSUPPORTED_CURRENCY_CODE,
                    StringUtils.collectionToDelimitedString(currencyCodes, ", ")));
        }
        return Stream.empty();
    }

    protected Stream<ValidationMessage> validateLanguageCode(String fieldName, String value, List<String> options) {
        List<String> languageCodes = CollectionUtils.isEmpty(options) ? Stream.of(Language.values()).map(Language::name).toList() : options;
        if (!languageCodes.contains(value)) {
            return Stream.of(new ValidationMessage(
                    fieldName,
                    value,
                    String.format(
                            "Field '%s' is an unsupported language code: %s only are supported (rejected value: '%s')",
                            fieldName,
                            StringUtils.collectionToDelimitedString(languageCodes, ", "),
                            value
                    ),
                    ValidationMessage.UNSUPPORTED_LANGUAGE_CODE,
                    StringUtils.collectionToDelimitedString(languageCodes, ", "))
            );
        }
        return Stream.empty();
    }

    private boolean validateByRegex(String regex, String value) {
        return Pattern.compile(regex).matcher(value).matches();
    }

}
