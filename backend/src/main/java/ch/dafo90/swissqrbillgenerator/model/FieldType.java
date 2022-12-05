package ch.dafo90.swissqrbillgenerator.model;

import lombok.Getter;

@Getter
public enum FieldType {

    STRING, IBAN, REFERENCE, TEXT, EMAIL, URL, NUMBER, CURRENCY_CODE, LANGUAGE_CODE, COUNTRY_CODE

}
