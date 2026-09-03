package com.dtp.cosmemgt.sales.promotion.entity;

import com.dtp.cosmemgt.sales.promotion.enums.TargetType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "promotion_target_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionTargetItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id", nullable = false)
    private Promotion promotion;

    @Column(name = "target_id", nullable = false)
    private String targetId;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type")
    private TargetType targetType;
}