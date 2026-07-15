package com.dtp.cosmemgt.catalog.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PricingRuleResponse {
    String name;
    String targetABCClass;
    int stockThreshold;
    BigDecimal priceMultiplier;
}
