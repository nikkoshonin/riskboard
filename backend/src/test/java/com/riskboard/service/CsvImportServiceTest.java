package com.riskboard.service;

import com.riskboard.dto.ImportResultDto;
import com.riskboard.model.Counterparty;
import com.riskboard.model.LimitType;
import com.riskboard.model.RiskLimit;
import com.riskboard.repository.CounterpartyRepository;
import com.riskboard.repository.RiskLimitRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test d'integration leger (contexte Spring + H2 en memoire) qui verifie que
 * l'import CSV upserte correctement les donnees et isole les lignes en erreur.
 */
@SpringBootTest
@Transactional
class CsvImportServiceTest {

    @Autowired
    private CsvImportService csvImportService;

    @Autowired
    private CounterpartyRepository counterpartyRepository;

    @Autowired
    private RiskLimitRepository riskLimitRepository;

    @Test
    void importCsv_shouldImportValidLinesAndReportErrors() {
        // Prepare
        String csv = """
                name,ricosCode,country,sector,limitType,maxAmount,usedAmount,currency
                BNP PARIBAS,RICOS48213,FR,Banking,CREDIT,50000000,32000000,EUR
                DEUTSCHE BANK AG,RICOS72905,DE,Banking,MARKET,20000000,18500000,EUR
                INVALID ROW,RICOS00000,FR,Banking,NOT_A_TYPE,1000,500,EUR
                """;

        // Act
        ImportResultDto result = csvImportService.importCsv(
                new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8)));

        //Assert
        assertThat(result.getSuccessCount()).isEqualTo(2);
        assertThat(result.getErrorCount()).isEqualTo(1);
        assertThat(result.getErrors().get(0).getMessage()).contains("limitType");

        assertThat(counterpartyRepository.findByRicosCode("RICOS48213")).isPresent();
        assertThat(counterpartyRepository.findByRicosCode("RICOS00000")).isEmpty();
    }

    @Test
    void importCsv_shouldUpsertExistingCounterpartyOnReimport() {
        // Prepare
        String firstImport = """
                name,ricosCode,country,sector,limitType,maxAmount,usedAmount,currency
                BNP PARIBAS,RICOS48213,FR,Banking,CREDIT,50000000,32000000,EUR
                """;
        csvImportService.importCsv(new ByteArrayInputStream(firstImport.getBytes(StandardCharsets.UTF_8)));

        String secondImport = """
                name,ricosCode,country,sector,limitType,maxAmount,usedAmount,currency
                BNP PARIBAS,RICOS48213,FR,Banking,CREDIT,50000000,45000000,EUR
                """;

        // Act
        ImportResultDto result = csvImportService.importCsv(
                new ByteArrayInputStream(secondImport.getBytes(StandardCharsets.UTF_8)));
        // Assert
        assertThat(result.getSuccessCount()).isEqualTo(1);

        Counterparty counterparty = counterpartyRepository.findByRicosCode("RICOS48213").orElseThrow();
        RiskLimit riskLimit = riskLimitRepository.findByCounterpartyAndLimitType(counterparty, LimitType.CREDIT).orElseThrow();
        assertThat(riskLimit.getUsedAmount()).isEqualByComparingTo("45000000");
    }

}
