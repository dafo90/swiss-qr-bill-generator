package ch.dafo90.swissqrbillgenerator.service;

import ch.dafo90.swissqrbillgenerator.mapper.GroupMapper;
import ch.dafo90.swissqrbillgenerator.model.Group;
import ch.dafo90.swissqrbillgenerator.property.AppProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FieldGroupService {

    private final AppProperties appProperties;
    private final GroupMapper groupMapper;

    public List<Group> getAll() {
        return groupMapper.toGroups(appProperties.groups());
    }

}
