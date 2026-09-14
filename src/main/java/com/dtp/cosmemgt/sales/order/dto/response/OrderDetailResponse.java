package com.dtp.cosmemgt.sales.order.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderDetailResponse {
    String id;
    List<OrderDetailItemsResponse> orderDetailItemsResponses;
    OrderShippingResponse orderShippingResponse;
}
