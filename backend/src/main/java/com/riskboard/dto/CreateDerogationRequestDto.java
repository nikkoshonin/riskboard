package com.riskboard.dto;

import com.riskboard.model.LimitType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CreateDerogationRequestDto {

    @NotNull(message = "La contrepartie est obligatoire")
    private Long counterpartyId;

    @NotNull(message = "Le type de risque est obligatoire")
    private LimitType limitType;

    @NotNull(message = "Le montant demande est obligatoire")
    @DecimalMin(value = "0.01", message = "Le montant demande doit etre strictement superieur a 0")
    private BigDecimal amount;

    @NotNull(message = "La raison est obligatoire")
    @Size(min = 20, message = "La raison doit contenir au moins 20 caracteres")
    private String reason;

    @NotNull(message = "Le champ 'Demande par' est obligatoire")
    @Size(min = 6, message = "Le champ 'Demande par' doit contenir au moins 6 caracteres")
    private String requestedBy;
}
