package com.riskboard.dto;

import com.riskboard.model.DerogationStatus;
import com.riskboard.model.LimitType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DerogationRequestDto {

    private Long id;
    private Long counterpartyId;
    private String counterpartyName;
    private LimitType limitType;
    private String requestedBy;
    private BigDecimal amount;
    private String reason;
    private DerogationStatus status;
    private LocalDateTime createdAt;
}
