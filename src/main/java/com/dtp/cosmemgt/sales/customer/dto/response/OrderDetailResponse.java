package com.dtp.cosmemgt.sales.customer.dto.response;

import com.dtp.cosmemgt.admin.entity.User;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderResponse {
    BigDecimal totalAmount;
    String channel;
    String paymentMethod;
    String orderStatus;
}
