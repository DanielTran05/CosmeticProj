package com.dtp.cosmemgt.sales.promotion.dto.request;

import com.dtp.cosmemgt.sales.promotion.enums.DiscountType;
import com.dtp.cosmemgt.sales.promotion.enums.ScopeType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
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

    @NotBlank(message = "PROMOTION_CODE_REQUIRED")
    @Size(min = 2, max = 50, message = "PROMOTION_CODE_INVALID_LENGTH")
    String code;

    @NotBlank(message = "PROMOTION_NAME_REQUIRED")
    String name;

    @NotNull(message = "DISCOUNT_TYPE_REQUIRED")
    DiscountType discountType;

    @NotNull(message = "DISCOUNT_VALUE_REQUIRED")
    @DecimalMin(value = "0.0", inclusive = false, message = "DISCOUNT_VALUE_MUST_BE_POSITIVE")
    BigDecimal discountValue;

    @NotNull(message = "SCOPE_TYPE_REQUIRED")
    ScopeType scopeType;

    @NotNull(message = "MIN_ORDER_AMOUNT_REQUIRED")
    @DecimalMin(value = "0.0", inclusive = true, message = "MIN_ORDER_AMOUNT_INVALID")
    BigDecimal minOrderAmount;

    @Min(value = 1, message = "USAGE_LIMIT_INVALID")
    Integer usageLimit;

    Boolean isAutoApplied;

    Boolean isActive;

    @NotNull(message = "START_DATE_REQUIRED")
    LocalDateTime startDate;

    @NotNull(message = "END_DATE_REQUIRED")
    @Future(message = "END_DATE_MUST_BE_FUTURE")
    LocalDateTime endDate;

    List<@Valid TargetItemRequest> targetItems;
}