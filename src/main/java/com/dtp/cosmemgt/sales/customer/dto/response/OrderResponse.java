package com.dtp.cosmemgt.sales.customer.dto.response;

import com.dtp.cosmemgt.admin.entity.User;
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
public class OrderResponse {
    String id;
    BigDecimal totalAmount;
    String orderStatus;
    LocalDateTime createdAt;
    OrderShippingResponse orderShipping;
}
