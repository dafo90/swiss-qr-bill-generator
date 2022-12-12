package ch.dafo90.swissqrbillgenerator.service;

import ch.dafo90.swissqrbillgenerator.exception.ValidationException;
import ch.dafo90.swissqrbillgenerator.mapper.GroupMapper;
import ch.dafo90.swissqrbillgenerator.model.FieldGroup;
import ch.dafo90.swissqrbillgenerator.model.FieldType;
import ch.dafo90.swissqrbillgenerator.model.csv.HeaderMap;
import ch.dafo90.swissqrbillgenerator.model.csv.Row;
import ch.dafo90.swissqrbillgenerator.model.validation.ValidationMessage;
import ch.dafo90.swissqrbillgenerator.property.AppProperties;
import ch.dafo90.swissqrbillgenerator.property.FieldGroupProperties;
import ch.dafo90.swissqrbillgenerator.property.FieldProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FieldGroupService {

    private final AppProperties appProperties;
    private final GroupMapper groupMapper;

    public List<FieldGroup> getAll() {
        return groupMapper.toGroups(appProperties.groups());
    }

    public String getField(String fieldName, Row row, List<HeaderMap> headersMap) {
        FieldProperties fieldProperties = getFieldPropertiesByName(fieldName);
        Optional<HeaderMap> headerMapOptional = getHeaderMap(headersMap, fieldName);

        return headerMapOptional.map(
                headerMap -> sanitizeValue(getValue(row.getCells().get(headerMap.getMapWithIndex()), headerMap.getStaticValue(), fieldProperties.defaultValue()), fieldProperties.type())
        ).orElseGet(() -> {
            log.debug("Field '{}' not mapped", fieldName);
            return getValue(null, null, fieldProperties.defaultValue());
        });
    }

    private Optional<HeaderMap> getHeaderMap(List<HeaderMap> headersMap, String fieldName) {
        return headersMap.stream().filter(headerMap -> headerMap.getFieldName().equals(fieldName)).findFirst();
    }

    protected String getValue(String value, String staticValue, String defaultValue) {
        if (StringUtils.hasText(value)) {
            return value.trim();
        }
        if (StringUtils.hasText(staticValue)) {
            return staticValue.trim();
        }
        if (StringUtils.hasText(defaultValue)) {
            return defaultValue.trim();
        }
        return null;
    }

    public FieldProperties getFieldPropertiesByName(String fieldName) {
        return appProperties.groups().stream()
                .map(FieldGroupProperties::fields)
                .flatMap(List::stream)
                .filter(field -> field.name().equals(fieldName))
                .findFirst()
                .orElseThrow(() -> new ValidationException(List.of(
                        new ValidationMessage(
                                fieldName,
                                null,
                                String.format("Field '%s' not configured in application.yml", fieldName),
                                ValidationMessage.INVALID_FIELD_NAME)


                )));
    }

    protected String sanitizeValue(String value, FieldType fieldType) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return switch (fieldType) {
            case STRING, URL, EMAIL -> value;
            case TEXT -> value.replace("\n", "<br/>");
            case NUMBER -> value.replaceAll("[^\\d\\.-]", "");
            default -> value.replace(" ", "").toUpperCase();
        };
    }

}
