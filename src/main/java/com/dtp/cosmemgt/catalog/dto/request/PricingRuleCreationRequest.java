package com.dtp.cosmemgt.catalog.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PricingRuleCreationRequest {
    @NotBlank
    @Size(min = 1, max = 50)
    String name;

    @NotBlank
    @Size(min = 1, max = 1)
    String targetABCClass;

    @NotNull
    @Size(min = 1, max = 6)
    Integer stockThreshold;

    @NotNull
    @Size(min = 1, max = 3)
    BigDecimal priceMultiplier;

    @NotNull
    Boolean isActive;
}
