package com.dtp.cosmemgt.catalog.dto.response;

import com.dtp.cosmemgt.catalog.entity.ProductVariant;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductVariantResponse {
    String id;
    String variantName;
    BigDecimal unitPrice;
    String img;
    String oum;
    String sku;
    private BigDecimal originalPrice;       //unitPrice before any discount is applied
    private BigDecimal discountedPrice;
    private String appliedPromotionCode;
}