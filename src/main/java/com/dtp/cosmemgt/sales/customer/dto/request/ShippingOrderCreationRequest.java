package com.dtp.cosmemgt.sales.customer.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ShippingOrderCreationRequest {
    String receiverName;
    String receiverPhone;
    String receiverAddress;
}
