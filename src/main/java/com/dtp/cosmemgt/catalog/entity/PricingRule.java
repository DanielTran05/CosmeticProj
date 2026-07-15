package com.dtp.cosmemgt.catalog.entity;

import com.dtp.cosmemgt.core.baseEntity.BaseAuditEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "pricing_rule")
@SQLDelete(sql = "UPDATE pricing_rule SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class PricingRule extends BaseAuditEntity {
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