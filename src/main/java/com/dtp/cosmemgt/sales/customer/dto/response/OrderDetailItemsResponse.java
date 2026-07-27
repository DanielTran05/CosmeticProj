package com.dtp.cosmemgt.sales.customer.dto.response;

import com.dtp.cosmemgt.catalog.cus.dto.response.ProductVariantResponse;
import com.dtp.cosmemgt.catalog.entity.ProductVariant;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderDetailItemsResponse {
    int qty;
    BigDecimal purchasedPrice;
    ProductVariantResponse productVariant;
}
