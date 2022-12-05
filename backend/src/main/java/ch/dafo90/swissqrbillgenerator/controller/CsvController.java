package ch.dafo90.swissqrbillgenerator.controller;

import ch.dafo90.swissqrbillgenerator.model.csv.Csv;
import ch.dafo90.swissqrbillgenerator.service.CsvService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/csv")
@RequiredArgsConstructor
public class CsvController {

    private final CsvService csvService;

    @PostMapping("/convert")
    public ResponseEntity<Csv> convert(
            @RequestParam(value = "separator", defaultValue = ",") char separator,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(csvService.convert(file, separator));
    }

}
