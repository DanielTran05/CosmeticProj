package com.dtp.cosmemgt.sales.order.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderShippingResponse {
    String trackingNumber;
    String receiverName;
    String receiverPhone;
    String receiverAddress;
    String shippingProvider;
}
