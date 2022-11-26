package ch.dafo90.swissqrbillgenerator.mapper;

import ch.dafo90.swissqrbillgenerator.model.Group;
import ch.dafo90.swissqrbillgenerator.property.GroupProperties;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, builder = @Builder(disableBuilder = true), uses = {FieldMapper.class})
public abstract class GroupMapper {

    public abstract Group toGroup(GroupProperties groupProperties);

    public abstract List<Group> toGroups(List<GroupProperties> groupPropertiesList);

}
