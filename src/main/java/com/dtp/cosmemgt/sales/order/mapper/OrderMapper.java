package com.dtp.cosmemgt.sales.order.mapper;

import com.dtp.cosmemgt.catalog.cus.dto.response.ProductVariantResponse;
import com.dtp.cosmemgt.catalog.entity.ProductVariant;
import com.dtp.cosmemgt.sales.order.dto.response.OrderDetailItemsResponse;
import com.dtp.cosmemgt.sales.order.dto.response.OrderDetailResponse;
import com.dtp.cosmemgt.sales.order.dto.response.OrderResponse;
import com.dtp.cosmemgt.sales.order.entity.Order;
import com.dtp.cosmemgt.sales.order.entity.OrderDetail;
import org.mapstruct.*;

@Mapper(componentModel = "spring",
        builder = @Builder(disableBuilder = true),
        uses = {OrderShippingMapper.class})
public interface OrderMapper {
    @Mapping(target = "orderShipping", source = "orderShipping")
    OrderResponse toOrderResponse(Order oder);

    @Mapping(target = "orderDetailItemsResponses", source = "orderDetails")
    @Mapping(target = "orderShippingResponse", source = "orderShipping")
    @Named("toOrderDetailResponse")
    OrderDetailResponse toOrderDetailResponse(Order order);                             //↓

    OrderDetailItemsResponse toOrderDetailItemsResponse(OrderDetail orderDetail);       //↓

    @Mapping(source = "id", target = "id")
    @Mapping(source = "variantName", target = "variantName")
    ProductVariantResponse toProductVariantResponse (ProductVariant productVariant);    //↓
}