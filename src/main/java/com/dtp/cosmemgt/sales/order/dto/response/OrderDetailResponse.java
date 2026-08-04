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
    String id;  //id cua order
    List<OrderDetailItemsResponse> orderDetailItemsResponses;   //list cac san pham cua no
    OrderShippingResponse orderShippingResponse;
}
