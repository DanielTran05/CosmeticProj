package com.dtp.cosmemgt.sales.internal.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderShippingResponse {
    String receiverName;
    String receiverPhone;
    String receiverAddress;
    String note;
}
