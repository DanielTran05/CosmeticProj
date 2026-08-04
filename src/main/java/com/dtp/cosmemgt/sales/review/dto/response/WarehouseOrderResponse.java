package com.dtp.cosmemgt.sales.review.dto.response;

import com.dtp.cosmemgt.admin.dto.response.UserResponse;
import com.dtp.cosmemgt.sales.order.dto.response.OrderDetailResponse;
import com.dtp.cosmemgt.sales.order.dto.response.OrderShippingResponse;
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
