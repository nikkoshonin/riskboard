package com.riskboard.repository;

import com.riskboard.model.Counterparty;
import com.riskboard.model.LimitType;
import com.riskboard.model.RiskLimit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RiskLimitRepository extends JpaRepository<RiskLimit, Long> {

    Optional<RiskLimit> findByCounterpartyAndLimitType(Counterparty counterparty, LimitType limitType);

    List<RiskLimit> findByLimitType(LimitType limitType);
}
