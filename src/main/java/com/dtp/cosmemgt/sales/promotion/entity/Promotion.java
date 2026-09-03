package com.dtp.cosmemgt.sales.promotion.entity;

import com.dtp.cosmemgt.core.baseEntity.BaseAuditEntity;
import com.dtp.cosmemgt.sales.promotion.enums.DiscountType;
import com.dtp.cosmemgt.sales.promotion.enums.ScopeType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "promotion")
@SQLDelete(sql = "UPDATE promotion SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Promotion extends BaseAuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(unique = true, nullable = false, length = 50)
    private String code;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DiscountType discountType;

    @Column(nullable = false)
    private BigDecimal discountValue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScopeType scopeType;

    @Column(nullable = false)
    private BigDecimal minOrderAmount;

    private Integer usageLimit;

    @Builder.Default
    private Integer usedCount = 0;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isActive = false; // true = đang active, false = đã hết hạn hoặc bị admin tắt

    @Builder.Default
    @Column(nullable = false)
    private Boolean isAutoApplied = false; // true = Flash Sale tự áp, false = phải nhập Code

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @OneToMany(mappedBy = "promotion", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PromotionTargetItem> targetItems = new ArrayList<>();

    String description;
}