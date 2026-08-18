package com.riskboard.controller;

import com.riskboard.dto.CreateDerogationRequestDto;
import com.riskboard.dto.DerogationRequestDto;
import com.riskboard.dto.LimitCheckDto;
import com.riskboard.model.DerogationStatus;
import com.riskboard.model.LimitType;
import com.riskboard.service.DerogationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/derogations")
public class DerogationController {

    private final DerogationService derogationService;

    public DerogationController(DerogationService derogationService) {
        this.derogationService = derogationService;
    }

    /** Utilise par le validator asynchrone du formulaire Angular. */
    @GetMapping("/check-limit")
    public LimitCheckDto checkLimit(@RequestParam Long counterpartyId, @RequestParam LimitType limitType) {
        return derogationService.checkLimit(counterpartyId, limitType);
    }

    @PostMapping
    public ResponseEntity<DerogationRequestDto> create(@Valid @RequestBody CreateDerogationRequestDto request) {
        DerogationRequestDto created = derogationService.createDerogationRequest(request);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/pending")
    public List<DerogationRequestDto> getPending() {
        return derogationService.getPendingRequests();
    }

    @GetMapping
    public List<DerogationRequestDto> getAll() {
        return derogationService.getAllRequests();
    }

    @PutMapping("/{id}/approve")
    public DerogationRequestDto approve(@PathVariable Long id) {
        return derogationService.updateStatus(id, DerogationStatus.APPROVED);
    }

    @PutMapping("/{id}/reject")
    public DerogationRequestDto reject(@PathVariable Long id) {
        return derogationService.updateStatus(id, DerogationStatus.REJECTED);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
    }
}
