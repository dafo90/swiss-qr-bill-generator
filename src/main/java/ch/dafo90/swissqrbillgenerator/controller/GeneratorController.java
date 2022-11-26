package ch.dafo90.swissqrbillgenerator.controller;

import ch.dafo90.swissqrbillgenerator.model.BillData;
import ch.dafo90.swissqrbillgenerator.service.BillPdfGeneratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
@RequestMapping("/api/v1/generate")
@RequiredArgsConstructor
public class GeneratorController {

    private final BillPdfGeneratorService billPdfGeneratorService;

    @PostMapping(value = "/bills", produces = "application/zip")
    public ResponseEntity<byte[]> bills(@RequestBody BillData billData) {
        HttpHeaders headers = new HttpHeaders();
        String zipFileName = String.format("%s_bills.zip", new Date().getTime());
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + zipFileName + "\"");
        return new ResponseEntity<>(billPdfGeneratorService.generateZippedBills(billData), headers, HttpStatus.OK);
    }

}
