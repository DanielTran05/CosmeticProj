package com.dtp.cosmemgt.catalog.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductSaleResponse {
    String productId;
    String slug;
    String productName;
    String thumbnail;
    BigDecimal originalMinPrice;
    BigDecimal discountedMinPrice;
    String discountBadge;
}