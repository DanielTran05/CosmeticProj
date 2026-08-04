package com.dtp.cosmemgt.sales.order.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderCreationRequest {
    List<OrderDetailRequest> orderDetailRequests;
    String paymentMethod;
    ShippingOrderCreationRequest shippingOrderCreationRequest;
    String note;
}
