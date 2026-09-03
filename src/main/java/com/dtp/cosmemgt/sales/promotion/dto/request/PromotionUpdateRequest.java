package com.dtp.cosmemgt.sales.promotion.dto.request;

import com.dtp.cosmemgt.sales.promotion.enums.DiscountType;
import com.dtp.cosmemgt.sales.promotion.enums.ScopeType;
import jakarta.validation.constraints.Min;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PromotionUpdateRequest {
    String code;
    
    DiscountType discountType;

    @Min(0)
    BigDecimal discountValue;

    ScopeType scopeType;

    @Min(0)
    BigDecimal minOrderAmount;

    @Min(1)
    Integer usageLimit;

    Boolean isAutoApplied;

    LocalDateTime startDate;

    LocalDateTime endDate;
    
    List<TargetItemRequest> targetItems = new ArrayList<>();
}