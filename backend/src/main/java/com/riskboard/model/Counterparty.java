package com.riskboard.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
@NoArgsConstructor
@Setter @Getter
@Entity
@Table(name = "counterparty", uniqueConstraints = @UniqueConstraint(columnNames = "ricosCode"))
public class Counterparty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String ricosCode;

    @Column(nullable = false)
    private String country;

    @Column(nullable = false)
    private String sector;

    @OneToMany(mappedBy = "counterparty", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RiskLimit> riskLimits = new ArrayList<>();

    public Counterparty(String name, String ricosCode, String country, String sector) {
        this.name = name;
        this.ricosCode = ricosCode;
        this.country = country;
        this.sector = sector;
    }
}
