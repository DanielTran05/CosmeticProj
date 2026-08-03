package com.dtp.cosmemgt.sales.customer.dto.response;

import com.dtp.cosmemgt.admin.entity.User;
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
