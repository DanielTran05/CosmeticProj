package com.dtp.cosmemgt.sales.order.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CartCalculateResponse {
    BigDecimal subTotal;        // total after discounted
    BigDecimal voucherDiscount;
    BigDecimal finalTotal;
    String appliedVoucherCode;
    String errorMsg;
}