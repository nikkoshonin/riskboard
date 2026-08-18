package com.riskboard.controller;

import com.riskboard.dto.CounterpartyDto;
import com.riskboard.dto.RiskLimitDto;
import com.riskboard.dto.SectorExposureDto;
import com.riskboard.model.LimitType;
import com.riskboard.repository.CounterpartyRepository;
import com.riskboard.service.RiskCalculationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/risk-limits")
public class RiskLimitController {

    private final RiskCalculationService riskCalculationService;
    private final CounterpartyRepository counterpartyRepository;

    public RiskLimitController(RiskCalculationService riskCalculationService,
                                CounterpartyRepository counterpartyRepository) {
        this.riskCalculationService = riskCalculationService;
        this.counterpartyRepository = counterpartyRepository;
    }

    @GetMapping
    public List<RiskLimitDto> getAll() {
        return riskCalculationService.getAllRiskLimits();
    }

    @GetMapping("/exposure-by-sector")
    public List<SectorExposureDto> getExposureBySector(@RequestParam LimitType limitType) {
        return riskCalculationService.getExposureBySector(limitType);
    }

    @GetMapping("/counterparties")
    public List<CounterpartyDto> getCounterparties() {
        return counterpartyRepository.findAll().stream()
                .map(c -> new CounterpartyDto(c.getId(), c.getName(), c.getRicosCode(), c.getCountry(), c.getSector()))
                .collect(Collectors.toList());
    }
}
