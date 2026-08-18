package com.riskboard.service;

import com.riskboard.dto.SectorExposureDto;
import com.riskboard.model.AlertLevel;
import com.riskboard.model.Counterparty;
import com.riskboard.model.LimitType;
import com.riskboard.model.RiskLimit;
import com.riskboard.repository.RiskLimitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RiskCalculationServiceTest {

    @Mock
    private RiskLimitRepository riskLimitRepository;

    private RiskCalculationService service;

    @BeforeEach
    void setUp() {
        service = new RiskCalculationService(riskLimitRepository);
    }

    @Test
    @DisplayName("usageRate < 70% -> GREEN")
    void alertLevel_shouldBeGreen_whenUsageRateBelow70() {
        BigDecimal usageRate = service.computeUsageRate(BigDecimal.valueOf(32_000_000), BigDecimal.valueOf(50_000_000));

        assertThat(usageRate).isEqualByComparingTo("64.00");
        assertThat(service.computeAlertLevel(usageRate)).isEqualTo(AlertLevel.GREEN);
    }

    @Test
    @DisplayName("70% <= usageRate <= 90% -> ORANGE")
    void alertLevel_shouldBeOrange_whenUsageRateBetween70And90() {
        BigDecimal usageRate = service.computeUsageRate(BigDecimal.valueOf(16_000_000), BigDecimal.valueOf(20_000_000));

        assertThat(usageRate).isEqualByComparingTo("80.00");
        assertThat(service.computeAlertLevel(usageRate)).isEqualTo(AlertLevel.ORANGE);
    }

    @Test
    @DisplayName("Bornes exactes 70% et 90% -> ORANGE (inclusif)")
    void alertLevel_shouldBeOrange_atExactBoundaries() {
        assertThat(service.computeAlertLevel(BigDecimal.valueOf(70))).isEqualTo(AlertLevel.ORANGE);
        assertThat(service.computeAlertLevel(BigDecimal.valueOf(90))).isEqualTo(AlertLevel.ORANGE);
    }

    @Test
    @DisplayName("usageRate > 90% -> RED")
    void alertLevel_shouldBeRed_whenUsageRateAbove90() {
        BigDecimal usageRate = service.computeUsageRate(BigDecimal.valueOf(18_500_000), BigDecimal.valueOf(20_000_000));

        assertThat(usageRate).isEqualByComparingTo("92.50");
        assertThat(service.computeAlertLevel(usageRate)).isEqualTo(AlertLevel.RED);
    }

    @Test
    @DisplayName("Exposition agregee par secteur pour un type de limite donne")
    void getExposureBySector_shouldAggregateUsedAmountsPerSector() {
        Counterparty bnp = new Counterparty("BNP PARIBAS", "RICOS48213", "FR", "Banking");
        Counterparty sg = new Counterparty("SOCIETE GENERALE", "RICOS91427", "FR", "Banking");
        Counterparty total = new Counterparty("TOTALENERGIES SE", "RICOS05364", "FR", "Energy");

        RiskLimit bnpCredit = new RiskLimit(bnp, LimitType.CREDIT, BigDecimal.valueOf(50_000_000),
                BigDecimal.valueOf(32_000_000), "EUR", LocalDateTime.now());
        RiskLimit sgCredit = new RiskLimit(sg, LimitType.CREDIT, BigDecimal.valueOf(30_000_000),
                BigDecimal.valueOf(29_500_000), "EUR", LocalDateTime.now());
        RiskLimit totalCredit = new RiskLimit(total, LimitType.CREDIT, BigDecimal.valueOf(25_000_000),
                BigDecimal.valueOf(15_000_000), "EUR", LocalDateTime.now());

        when(riskLimitRepository.findByLimitType(LimitType.CREDIT))
                .thenReturn(List.of(bnpCredit, sgCredit, totalCredit));

        List<SectorExposureDto> exposures = service.getExposureBySector(LimitType.CREDIT);

        assertThat(exposures).hasSize(2);

        SectorExposureDto banking = exposures.stream()
                .filter(e -> e.getSector().equals("Banking"))
                .findFirst().orElseThrow();
        assertThat(banking.getTotalUsedAmount()).isEqualByComparingTo("61500000");

        SectorExposureDto energy = exposures.stream()
                .filter(e -> e.getSector().equals("Energy"))
                .findFirst().orElseThrow();
        assertThat(energy.getTotalUsedAmount()).isEqualByComparingTo("15000000");
    }

    @Test
    @DisplayName("maxAmount = 0 -> usageRate = 0 (pas de division par zero)")
    void computeUsageRate_shouldReturnZero_whenMaxAmountIsZero() {
        BigDecimal usageRate = service.computeUsageRate(BigDecimal.valueOf(1000), BigDecimal.ZERO);
        assertThat(usageRate).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
