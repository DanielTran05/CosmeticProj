package com.dtp.cosmemgt.sales.order.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CartCalculateRequest {
    @NotEmpty
    List<OrderDetailRequest> items; // variantId, qty
    String voucherCode; // ScopeType.ORDER
}
