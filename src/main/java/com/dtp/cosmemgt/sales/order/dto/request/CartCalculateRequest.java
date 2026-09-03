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
    private List<OrderDetailRequest> items; // Gồm: productVariantId và qty
    private String voucherCode; // Mã ScopeType.ORDER khách nhập (có thể null)
}
