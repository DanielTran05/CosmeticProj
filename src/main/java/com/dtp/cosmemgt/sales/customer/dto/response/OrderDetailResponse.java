package com.dtp.cosmemgt.sales.customer.dto.response;

import com.dtp.cosmemgt.catalog.cus.dto.response.ProductVariantResponse;
import com.dtp.cosmemgt.catalog.internal.dto.response.AdminProductVariantResponse;
import com.dtp.cosmemgt.sales.entity.OrderDetail;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderDetailResponse {
    String id;  //id cua order
    List<OrderDetailItemsResponse> orderDetailItemsResponses;   //list cac san pham cua no
}
