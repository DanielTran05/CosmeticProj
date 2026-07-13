package com.dtp.cosmemgt.catalog.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "pricing_rule")
public class PricingRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    @Basic(optional = false)
    @NotNull
    String name;

    @Basic(optional = false)
    @NotNull
    String targetABCClass;

    @Basic(optional = false)
    @NotNull
    String stockThreshold;

    BigDecimal priceMultiplier;
}