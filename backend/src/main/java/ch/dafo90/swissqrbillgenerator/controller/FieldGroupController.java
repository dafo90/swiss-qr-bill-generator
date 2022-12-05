package ch.dafo90.swissqrbillgenerator.controller;

import ch.dafo90.swissqrbillgenerator.model.FieldGroup;
import ch.dafo90.swissqrbillgenerator.service.FieldGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/field-groups")
@RequiredArgsConstructor
public class FieldGroupController {

    private final FieldGroupService fieldGroupService;

    @GetMapping
    public ResponseEntity<List<FieldGroup>> getAll() {
        return ResponseEntity.ok(fieldGroupService.getAll());
    }

}
