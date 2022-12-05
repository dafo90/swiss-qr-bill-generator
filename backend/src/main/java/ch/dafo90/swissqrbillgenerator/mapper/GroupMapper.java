package ch.dafo90.swissqrbillgenerator.mapper;

import ch.dafo90.swissqrbillgenerator.model.FieldGroup;
import ch.dafo90.swissqrbillgenerator.property.FieldGroupProperties;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, builder = @Builder(disableBuilder = true), uses = {FieldMapper.class})
public interface GroupMapper {

    FieldGroup toGroup(FieldGroupProperties fieldGroupProperties);

    List<FieldGroup> toGroups(List<FieldGroupProperties> fieldGroupPropertiesList);

}
