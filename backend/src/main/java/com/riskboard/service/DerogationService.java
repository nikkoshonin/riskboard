package com.riskboard.service;

import com.riskboard.dto.CreateDerogationRequestDto;
import com.riskboard.dto.DerogationRequestDto;
import com.riskboard.dto.LimitCheckDto;
import com.riskboard.model.Counterparty;
import com.riskboard.model.DerogationRequest;
import com.riskboard.model.DerogationStatus;
import com.riskboard.model.RiskLimit;
import com.riskboard.repository.CounterpartyRepository;
import com.riskboard.repository.DerogationRequestRepository;
import com.riskboard.repository.RiskLimitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DerogationService {

    /** Une derogation ne peut pas depasser 150% de la limite maximale existante. */
    private static final BigDecimal MAX_DEROGATION_FACTOR = BigDecimal.valueOf(1.5);

    private final DerogationRequestRepository derogationRequestRepository;
    private final CounterpartyRepository counterpartyRepository;
    private final RiskLimitRepository riskLimitRepository;

    public DerogationService(DerogationRequestRepository derogationRequestRepository,
                              CounterpartyRepository counterpartyRepository,
                              RiskLimitRepository riskLimitRepository) {
        this.derogationRequestRepository = derogationRequestRepository;
        this.counterpartyRepository = counterpartyRepository;
        this.riskLimitRepository = riskLimitRepository;
    }

    public LimitCheckDto checkLimit(Long counterpartyId, com.riskboard.model.LimitType limitType) {
        Optional<Counterparty> counterparty = counterpartyRepository.findById(counterpartyId);
        if (counterparty.isEmpty()) {
            return new LimitCheckDto(false, null, null);
        }

        Optional<RiskLimit> riskLimit = riskLimitRepository.findByCounterpartyAndLimitType(counterparty.get(), limitType);
        if (riskLimit.isEmpty()) {
            return new LimitCheckDto(false, null, null);
        }

        BigDecimal maxAmount = riskLimit.get().getMaxAmount();
        BigDecimal maxAllowed = maxAmount.multiply(MAX_DEROGATION_FACTOR);
        return new LimitCheckDto(true, maxAmount, maxAllowed);
    }

    @Transactional
    public DerogationRequestDto createDerogationRequest(CreateDerogationRequestDto request) {
        Counterparty counterparty = counterpartyRepository.findById(request.getCounterpartyId())
                .orElseThrow(() -> new IllegalArgumentException("Contrepartie introuvable : " + request.getCounterpartyId()));

        RiskLimit riskLimit = riskLimitRepository.findByCounterpartyAndLimitType(counterparty, request.getLimitType())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Aucune limite " + request.getLimitType() + " n'existe pour la contrepartie " + counterparty.getName()));

        BigDecimal maxAllowed = riskLimit.getMaxAmount().multiply(MAX_DEROGATION_FACTOR);
        if (request.getAmount().compareTo(maxAllowed) > 0) {
            throw new IllegalArgumentException(
                    "Le montant demande depasse 150% de la limite max (" + maxAllowed + ")");
        }

        DerogationRequest entity = new DerogationRequest();
        entity.setCounterparty(counterparty);
        entity.setLimitType(request.getLimitType());
        entity.setAmount(request.getAmount());
        entity.setReason(request.getReason());
        entity.setRequestedBy(request.getRequestedBy());
        entity.setStatus(DerogationStatus.PENDING);

        entity = derogationRequestRepository.save(entity);
        return toDto(entity);
    }

    @Transactional(readOnly = true)
    public List<DerogationRequestDto> getPendingRequests() {
        return derogationRequestRepository.findByStatus(DerogationStatus.PENDING).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DerogationRequestDto> getAllRequests() {
        return derogationRequestRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public DerogationRequestDto updateStatus(Long id, DerogationStatus status) {
        DerogationRequest entity = derogationRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Demande de derogation introuvable : " + id));
        entity.setStatus(status);
        entity = derogationRequestRepository.save(entity);
        return toDto(entity);
    }

    private DerogationRequestDto toDto(DerogationRequest entity) {
        return new DerogationRequestDto(
                entity.getId(),
                entity.getCounterparty().getId(),
                entity.getCounterparty().getName(),
                entity.getLimitType(),
                entity.getRequestedBy(),
                entity.getAmount(),
                entity.getReason(),
                entity.getStatus(),
                entity.getCreatedAt()
        );
    }
}
