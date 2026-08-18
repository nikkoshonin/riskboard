package com.riskboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor @NoArgsConstructor
@Getter @Setter
public class ImportErrorDto {

    private int lineNumber;
    private String rawLine;
    private String message;
}
