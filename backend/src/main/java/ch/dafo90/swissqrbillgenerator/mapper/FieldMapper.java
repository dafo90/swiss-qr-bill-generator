package ch.dafo90.swissqrbillgenerator.mapper;

import ch.dafo90.swissqrbillgenerator.model.Field;
import ch.dafo90.swissqrbillgenerator.property.FieldProperties;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, builder = @Builder(disableBuilder = true))
public interface FieldMapper {

    Field toField(FieldProperties fieldProperties);

    List<Field> toFields(List<FieldProperties> fieldPropertiesList);

}
