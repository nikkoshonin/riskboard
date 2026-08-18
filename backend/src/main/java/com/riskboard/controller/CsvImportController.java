package com.riskboard.controller;

import com.riskboard.dto.ImportResultDto;
import com.riskboard.service.CsvImportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/import")
public class CsvImportController {

    private final CsvImportService csvImportService;

    public CsvImportController(CsvImportService csvImportService) {
        this.csvImportService = csvImportService;
    }

    @PostMapping(value = "/csv", consumes = "multipart/form-data")
    public ResponseEntity<ImportResultDto> importCsv(@RequestParam("file") MultipartFile file) throws IOException {
        ImportResultDto result = csvImportService.importCsv(file.getInputStream());
        return ResponseEntity.ok(result);
    }
}
