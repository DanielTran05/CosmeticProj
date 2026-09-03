package com.dtp.cosmemgt.sales.promotion.dto.response;

import com.dtp.cosmemgt.sales.promotion.entity.PromotionTargetItem;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminPromotionDetailResponse {
    String id;
    String code;
    String name;
    String discountType;
    BigDecimal discountValue;
    String scopeType;
    BigDecimal minOrderAmount;
    Integer usageLimit;
    Integer usedCount;
    Boolean isAutoApplied;
    LocalDateTime startDate;
    LocalDateTime endDate;
    List<PromotionTargetItemsResponse> targetItems;
    String description;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    LocalDateTime deletedAt;
    String deletedBy;
    String createdBy;
}