package com.dtp.cosmemgt.sales.promotion.dto.request;

import com.dtp.cosmemgt.sales.promotion.enums.DiscountType;
import com.dtp.cosmemgt.sales.promotion.enums.ScopeType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
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
    @Size(min = 2, max = 50, message = "PROMOTION_CODE_INVALID_LENGTH")
    String code;

    DiscountType discountType;

    @DecimalMin(value = "0.0", inclusive = true, message = "DISCOUNT_VALUE_INVALID")
    BigDecimal discountValue;

    ScopeType scopeType;

    @DecimalMin(value = "0.0", inclusive = true, message = "MIN_ORDER_AMOUNT_INVALID")
    BigDecimal minOrderAmount;

    @Min(value = 1, message = "USAGE_LIMIT_INVALID")
    Integer usageLimit;

    Boolean isAutoApplied;

    LocalDateTime startDate;

    @Future(message = "END_DATE_MUST_BE_FUTURE")
    LocalDateTime endDate;

    List<@Valid TargetItemRequest> targetItems = new ArrayList<>();
}