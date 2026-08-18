package com.riskboard.repository;

import com.riskboard.model.DerogationRequest;
import com.riskboard.model.DerogationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DerogationRequestRepository extends JpaRepository<DerogationRequest, Long> {
    List<DerogationRequest> findByStatus(DerogationStatus status);
}
