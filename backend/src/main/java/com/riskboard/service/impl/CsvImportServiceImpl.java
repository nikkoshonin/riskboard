package com.riskboard.service.impl;

import com.riskboard.dto.ImportErrorDto;
import com.riskboard.dto.ImportResultDto;
import com.riskboard.model.Counterparty;
import com.riskboard.model.LimitType;
import com.riskboard.model.RiskLimit;
import com.riskboard.repository.CounterpartyRepository;
import com.riskboard.repository.RiskLimitRepository;
import com.riskboard.service.CsvImportService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/**
 * Parse le fichier CSV de contreparties/limites et upserte les entites
 * correspondantes. Format attendu (en-tete obligatoire) :
 *
 *   name,ricosCode,country,sector,limitType,maxAmount,usedAmount,currency
 *
 * Chaque ligne en erreur (colonne manquante, valeur non numerique, type de
 * limite invalide, etc.) est ignoree individuellement et reportee dans le
 * resultat, sans interrompre le reste de l'import.
 */
@Service
@RequiredArgsConstructor
public class CsvImportServiceImpl implements CsvImportService {

    private static final String[] EXPECTED_HEADERS = {
            "name", "ricosCode", "country", "sector", "limitType", "maxAmount", "usedAmount", "currency"
    };

    private final CounterpartyRepository counterpartyRepository;
    private final RiskLimitRepository riskLimitRepository;
    
    @Override
    public ImportResultDto importCsv(InputStream inputStream) {
        ImportResultDto result = new ImportResultDto();

        try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT.builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .setTrim(true)
                     .setIgnoreSurroundingSpaces(true)
                     .build()
                     .parse(reader)) {

            for (String expectedHeader : EXPECTED_HEADERS) {
                if (!parser.getHeaderNames().contains(expectedHeader)) {
                    ImportErrorDto error = new ImportErrorDto(0, "",
                            "En-tete manquant : '" + expectedHeader + "'. Import annule.");
                    result.addError(error);
                    return result;
                }
            }

            for (CSVRecord record : parser) {
                processRecord(record, result);
            }

        } catch (IOException e) {
            result.addError(new ImportErrorDto(0, "", "Impossible de lire le fichier CSV : " + e.getMessage()));
        }

        return result;
    }

    private void processRecord(CSVRecord record, ImportResultDto result) {
        long lineNumber = record.getRecordNumber() + 1; // increment record number

        try {
            String name = requireNonBlank(record.get("name"), "name");
            String ricosCode = requireNonBlank(record.get("ricosCode"), "ricosCode");
            String country = requireNonBlank(record.get("country"), "country");
            String sector = requireNonBlank(record.get("sector"), "sector");
            LimitType limitType = parseLimitType(record.get("limitType"));
            BigDecimal maxAmount = parseAmount(record.get("maxAmount"), "maxAmount");
            BigDecimal usedAmount = parseAmount(record.get("usedAmount"), "usedAmount");
            String currency = requireNonBlank(record.get("currency"), "currency");

            if (maxAmount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("maxAmount doit etre strictement positif");
            }
            if (usedAmount.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("usedAmount ne peut pas etre negatif");
            }

            Counterparty counterparty = counterpartyRepository.findByRicosCode(ricosCode)
                    .orElseGet(() -> new Counterparty(name, ricosCode, country, sector));
            counterparty.setName(name);
            counterparty.setCountry(country);
            counterparty.setSector(sector);
            counterparty = counterpartyRepository.save(counterparty);

            Counterparty finalCounterparty = counterparty;
            RiskLimit riskLimit = riskLimitRepository
                    .findByCounterpartyAndLimitType(counterparty, limitType)
                    .orElseGet(() -> new RiskLimit(finalCounterparty, limitType, maxAmount, usedAmount, currency, LocalDateTime.now()));

            riskLimit.setCounterparty(counterparty);
            riskLimit.setLimitType(limitType);
            riskLimit.setMaxAmount(maxAmount);
            riskLimit.setUsedAmount(usedAmount);
            riskLimit.setCurrency(currency);
            riskLimit.setLastUpdated(LocalDateTime.now());

            riskLimitRepository.save(riskLimit);
            result.incrementSuccess();

        } catch (Exception e) {
            result.addError(new ImportErrorDto((int) lineNumber, record.toString(), e.getMessage()));
        }
    }

    private String requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Le champ '" + fieldName + "' est vide ou manquant");
        }
        return value.trim();
    }

    private LimitType parseLimitType(String value) {
        String trimmed = requireNonBlank(value, "limitType");
        try {
            return LimitType.valueOf(trimmed.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Valeur invalide pour limitType : '" + trimmed + "' (attendu CREDIT, MARKET ou LIQUIDITY)");
        }
    }

    private BigDecimal parseAmount(String value, String fieldName) {
        String trimmed = requireNonBlank(value, fieldName);
        try {
            return new BigDecimal(trimmed);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Valeur numerique invalide pour '" + fieldName + "' : '" + trimmed + "'");
        }
    }
}
