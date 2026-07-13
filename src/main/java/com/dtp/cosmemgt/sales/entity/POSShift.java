package com.dtp.cosmemgt.sales.entity;

import com.dtp.cosmemgt.core.baseEntity.BaseAuditEntity;
import com.dtp.cosmemgt.sales.enums.POSShiftStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "pos_shift")
public class POSShift extends BaseAuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Basic(optional = false)
    @Column(precision = 19, scale = 4)
    @NotNull
    BigDecimal startingCash;

    @Builder.Default
    @Basic(optional = false)
    @Column(precision = 19, scale = 4)
    @NotNull
    BigDecimal cashSales = BigDecimal.ZERO;

    @Builder.Default
    @Basic(optional = false)
    @Column(precision = 19, scale = 4)
    @NotNull
    BigDecimal expectedCash = BigDecimal.ZERO;

    @Builder.Default
    @Basic(optional = false)
    @Column(precision = 19, scale = 4)
    @NotNull
    BigDecimal actualCash = BigDecimal.ZERO;

    @Builder.Default
    @Basic(optional = false)
    @Column(precision = 19, scale = 4)
    @NotNull
    BigDecimal discrepancy = BigDecimal.ZERO;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    POSShiftStatus status = POSShiftStatus.OPEN;

    @Builder.Default
    @NotNull
    LocalDateTime startTime = LocalDateTime.now();

    @Builder.Default
    @NotNull
    LocalDateTime endTime = LocalDateTime.now();
}