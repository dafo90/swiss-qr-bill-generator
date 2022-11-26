package ch.dafo90.swissqrbillgenerator.mapper;

import ch.dafo90.swissqrbillgenerator.model.Field;
import ch.dafo90.swissqrbillgenerator.property.FieldProperties;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, builder = @Builder(disableBuilder = true))
public abstract class FieldMapper {

    public abstract Field toField(FieldProperties fieldProperties);

    public abstract List<Field> toFields(List<FieldProperties> fieldPropertiesList);

}
