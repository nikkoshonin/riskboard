package com.riskboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ImportResultDto {

    private int successCount;
    private int errorCount;
    private List<ImportErrorDto> errors = new ArrayList<>();

    public void addError(ImportErrorDto error) {
        this.errors.add(error);
        this.errorCount++;
    }

    public void incrementSuccess() {
        this.successCount++;
    }
}
