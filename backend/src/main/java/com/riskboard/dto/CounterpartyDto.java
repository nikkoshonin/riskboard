package com.riskboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CounterpartyDto {
    private Long id;
    private String name;
    private String ricosCode;
    private String country;
    private String sector;
}
