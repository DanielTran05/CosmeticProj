package com.dtp.cosmemgt.sales.promotion.dto.request;

import com.dtp.cosmemgt.sales.promotion.enums.DiscountType;
import com.dtp.cosmemgt.sales.promotion.enums.ScopeType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PromotionCreationRequest {
    String code;

    String name;

    DiscountType discountType;

    BigDecimal discountValue;

    ScopeType scopeType;

    BigDecimal minOrderAmount;

    Integer usageLimit;

    Boolean isAutoApplied;

    Boolean isActive;

    LocalDateTime startDate;

    LocalDateTime endDate;

    List<TargetItemRequest> targetItems;
}