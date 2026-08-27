package com.dtp.cosmemgt.sales.order.dto.response;

import com.dtp.cosmemgt.catalog.dto.response.ProductVariantResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderDetailItemsResponse {
    String slug;
    String productName;
    int quantity; //test
    BigDecimal purchasedPrice;
    ProductVariantResponse productVariant;
}
