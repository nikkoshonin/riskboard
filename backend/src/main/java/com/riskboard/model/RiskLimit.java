package com.riskboard.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@NoArgsConstructor
@Setter
@Getter
@Entity
@Table(name = "risk_limit", uniqueConstraints = @UniqueConstraint(columnNames = {"counterparty_id", "limitType"}))
public class RiskLimit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "counterparty_id", nullable = false)
    private Counterparty counterparty;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LimitType limitType;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal maxAmount;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal usedAmount;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false)
    private LocalDateTime lastUpdated;

    public RiskLimit(Counterparty counterparty, LimitType limitType, BigDecimal maxAmount,
                      BigDecimal usedAmount, String currency, LocalDateTime lastUpdated) {
        this.counterparty = counterparty;
        this.limitType = limitType;
        this.maxAmount = maxAmount;
        this.usedAmount = usedAmount;
        this.currency = currency;
        this.lastUpdated = lastUpdated;
    }
}
