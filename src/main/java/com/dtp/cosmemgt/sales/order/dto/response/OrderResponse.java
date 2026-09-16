package com.dtp.cosmemgt.sales.order.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderResponse {
    String id;
    BigDecimal totalAmount;
    String orderStatus;
    String paymentStatus;
    String paymentMethod;
    String employeeName;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    OrderShippingResponse orderShipping;
    List<OrderDetailItemsResponse> orderDetailItemsResponses;
}