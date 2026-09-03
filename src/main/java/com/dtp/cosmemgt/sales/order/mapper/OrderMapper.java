package com.dtp.cosmemgt.sales.order.mapper;

import com.dtp.cosmemgt.catalog.dto.response.ProductVariantResponse;
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
    @Mapping(target = "orderDetailItemsResponses", source = "orderDetails") //test
    @Mapping(target = "orderShipping", source = "orderShipping")
    @Mapping(target = "employeeName", source = "employee.fullName")
    OrderResponse toOrderResponse(Order order);

    @Mapping(target = "orderDetailItemsResponses", source = "orderDetails")
    @Mapping(target = "orderShippingResponse", source = "orderShipping")
    @Named("toOrderDetailResponse")
    OrderDetailResponse toOrderDetailResponse(Order order);                             //↓

    @Mapping(source = "quantity", target = "quantity")
    @Mapping(source = "productVariant.product.name", target = "productName")
    @Mapping(source = "productVariant.product.slug", target = "slug")
    OrderDetailItemsResponse toOrderDetailItemsResponse(OrderDetail orderDetail);       //↓

    @Mapping(source = "id", target = "id")
    @Mapping(source = "variantName", target = "variantName")
    @Mapping(target = "discountedPrice", source = "discountedPrice")
    ProductVariantResponse toProductVariantResponse (ProductVariant productVariant);    //↓
}