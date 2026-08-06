package com.dtp.cosmemgt.catalog.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductDetailResponse {
    String id;
    String name;
    BigDecimal basePrice;
    String avatar;
    String description;
    List<ProductVariantResponse> productVariantResponses;
}
