package ch.dafo90.swissqrbillgenerator.property;

import ch.dafo90.swissqrbillgenerator.model.FieldType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
public record FieldProperties(
        @NotBlank String name,
        @NotBlank String defaultMap,
        @NotBlank String label,
        @NotNull Boolean required,
        String requiredText,
        @NotNull FieldType type,
        String defaultValue,
        List<String> options,
        @NotNull Boolean allowStaticValue) {

    // Document
    public static final String SALUTATION = "salutation";
    public static final String TEXT = "text";
    public static final String SIGNATURE_TITLE_1 = "signatureTitle1";
    public static final String SIGNATURE_NAME_1 = "signatureName1";
    public static final String SIGNATURE_TITLE_2 = "signatureTitle2";
    public static final String SIGNATURE_NAME_2 = "signatureName2";
    public static final String CLOSURE = "closure";

    // Bill
    public static final String ACCOUNT = "account";
    public static final String AMOUNT = "amount";
    public static final String CURRENCY = "currency";
    public static final String LANGUAGE = "language";
    public static final String REFERENCE = "reference";
    public static final String UNSTRUCTURED_MESSAGE = "unstructuredMessage";

    // Creditor
    public static final String CREDITOR_NAME = "creditorName";
    public static final String CREDITOR_STREET = "creditorStreet";
    public static final String CREDITOR_LOCALITY = "creditorLocality";
    public static final String CREDITOR_COUNTRY = "creditorCountry";
    public static final String CREDITOR_EMAIL = "creditorEmail";
    public static final String CREDITOR_WEBSITE = "creditorWebsite";
    public static final String CREDITOR_PHONE_NUMBER = "creditorPhoneNumber";

    // Debtor
    public static final String DEBTOR_TITLE = "debtorTitle";
    public static final String DEBTOR_ORGANISATION = "debtorOrganisation";
    public static final String DEBTOR_NAME = "debtorName";
    public static final String DEBTOR_STREET = "debtorStreet";
    public static final String DEBTOR_LOCALITY = "debtorLocality";
    public static final String DEBTOR_COUNTRY = "debtorCountry";

}
