package com.dtp.cosmemgt.sales.internal.dto.response;

import com.dtp.cosmemgt.admin.dto.response.UserResponse;
import com.dtp.cosmemgt.sales.customer.dto.response.OrderDetailResponse;
import com.dtp.cosmemgt.sales.entity.OrderShipping;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WarehouseOrderResponse {
    BigDecimal totalAmount;
    OrderDetailResponse orderDetails;
    UserResponse customer;
    OrderShippingResponse orderShipping;
    LocalDateTime createdAt;
}
