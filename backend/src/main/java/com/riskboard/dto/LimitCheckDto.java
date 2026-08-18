package com.riskboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Reponse du endpoint de verification utilise par le validator asynchrone
 * Angular : indique si une limite existe pour le couple contrepartie/type,
 * et si oui le montant maximal autorise pour une derogation (150% de maxAmount).
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class LimitCheckDto {

    private boolean limitExists;
    private BigDecimal maxAmount;
    private BigDecimal maxAllowedDerogationAmount;
}
