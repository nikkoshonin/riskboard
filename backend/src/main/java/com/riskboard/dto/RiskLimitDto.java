package com.riskboard.dto;

import com.riskboard.model.AlertLevel;
import com.riskboard.model.LimitType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Representation d'une limite de risque telle qu'exposee au frontend :
 * on y joint les informations de la contrepartie et le niveau d'alerte
 * calcule, pour eviter au client de recalculer usageRate lui-meme.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RiskLimitDto {

    private Long id;
    private Long counterpartyId;
    private String counterpartyName;
    private String sector;
    private LimitType limitType;
    private BigDecimal maxAmount;
    private BigDecimal usedAmount;
    private String currency;
    private BigDecimal usageRate;
    private AlertLevel alertLevel;
    private LocalDateTime lastUpdated;
}
