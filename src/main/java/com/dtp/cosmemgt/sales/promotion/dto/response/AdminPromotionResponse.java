package com.dtp.cosmemgt.sales.promotion.dto.response;

import com.dtp.cosmemgt.sales.promotion.entity.PromotionTargetItem;
import com.dtp.cosmemgt.sales.promotion.enums.DiscountType;
import com.dtp.cosmemgt.sales.promotion.enums.ScopeType;
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
public class AdminPromotionResponse {
    String id;
    String code;
    String name;
    String discountType;
    BigDecimal discountValue;
    String scopeType;       //filter
    Integer usageLimit;
    Integer usedCount;
    String description;
    List<PromotionTargetItemsResponse> targetItems;
    LocalDateTime startDate;
    LocalDateTime endDate;
    Boolean isActive;
}