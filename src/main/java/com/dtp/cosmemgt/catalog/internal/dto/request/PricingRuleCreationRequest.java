package com.dtp.cosmemgt.catalog.internal.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PricingRuleCreationRequest {
    String name;
    String targetABCClass;
    Integer stockThreshold;
    BigDecimal priceMultiplier;
    Boolean isActive;
}
