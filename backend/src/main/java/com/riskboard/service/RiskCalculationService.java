package com.riskboard.service;

import com.riskboard.dto.RiskLimitDto;
import com.riskboard.dto.SectorExposureDto;
import com.riskboard.model.AlertLevel;
import com.riskboard.model.LimitType;
import com.riskboard.model.RiskLimit;
import com.riskboard.repository.RiskLimitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Concentre toute la logique de calcul de risque : taux d'usage d'une limite,
 * niveau d'alerte associe, et exposition agregee par secteur.
 */
@Service
@RequiredArgsConstructor
public class RiskCalculationService {

    private final RiskLimitRepository riskLimitRepository;

    /**
     * usageRate = (usedAmount / maxAmount) * 100, arrondi a 2 decimales.
     * Si maxAmount vaut 0, on considere le taux d'usage comme 0 pour eviter
     * une division par zero (cas limite non couvert par l'enonce mais qui
     * pourrait survenir avec des donnees corrompues).
     */
    public BigDecimal computeUsageRate(BigDecimal usedAmount, BigDecimal maxAmount) {
        if (maxAmount == null || maxAmount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return usedAmount
                .divide(maxAmount, MathContext.DECIMAL64)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Niveau d'alerte :
     *  - usageRate < 70            -> GREEN
     *  - 70 <= usageRate <= 90     -> ORANGE
     *  - usageRate > 90            -> RED
     */
    public AlertLevel computeAlertLevel(BigDecimal usageRate) {
        BigDecimal seventy = BigDecimal.valueOf(70);
        BigDecimal ninety = BigDecimal.valueOf(90);

        if (usageRate.compareTo(seventy) < 0) {
            return AlertLevel.GREEN;
        }
        if (usageRate.compareTo(ninety) <= 0) {
            return AlertLevel.ORANGE;
        }
        return AlertLevel.RED;
    }

    public RiskLimitDto toDto(RiskLimit riskLimit) {
        BigDecimal usageRate = computeUsageRate(riskLimit.getUsedAmount(), riskLimit.getMaxAmount());
        AlertLevel alertLevel = computeAlertLevel(usageRate);

        return new RiskLimitDto(
                riskLimit.getId(),
                riskLimit.getCounterparty().getId(),
                riskLimit.getCounterparty().getName(),
                riskLimit.getCounterparty().getSector(),
                riskLimit.getLimitType(),
                riskLimit.getMaxAmount(),
                riskLimit.getUsedAmount(),
                riskLimit.getCurrency(),
                usageRate,
                alertLevel,
                riskLimit.getLastUpdated()
        );
    }

    @Transactional(readOnly = true)
    public List<RiskLimitDto> getAllRiskLimits() {
        return riskLimitRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Exposition agregee par secteur : somme des usedAmount de toutes les
     * limites, groupee par secteur de la contrepartie. Utilisee pour la
     * vue "toutes limites confondues".
     */
    public Map<String, BigDecimal> getExposureBySector() {
        return aggregateBySector(riskLimitRepository.findAll());
    }

    /**
     * Exposition agregee par secteur pour un type de limite donne (CREDIT /
     * MARKET / LIQUIDITY) : c'est cette vue qui alimente le tableau agrege
     * du frontend lorsqu'un type de limite est selectionne.
     */
    @Transactional(readOnly = true)
    public List<SectorExposureDto> getExposureBySector(LimitType limitType) {
        Map<String, BigDecimal> exposures = aggregateBySector(riskLimitRepository.findByLimitType(limitType));

        return exposures.entrySet().stream()
                .map(e -> new SectorExposureDto(e.getKey(), e.getValue()))
                .sorted(Comparator.comparing(SectorExposureDto::getSector))
                .collect(Collectors.toList());
    }

    private Map<String, BigDecimal> aggregateBySector(List<RiskLimit> riskLimits) {
        Map<String, BigDecimal> result = new TreeMap<>();
        for (RiskLimit riskLimit : riskLimits) {
            String sector = riskLimit.getCounterparty().getSector();
            result.merge(sector, riskLimit.getUsedAmount(), BigDecimal::add);
        }
        return result;
    }
}
