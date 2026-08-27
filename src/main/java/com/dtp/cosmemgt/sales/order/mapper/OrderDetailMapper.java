package com.dtp.cosmemgt.sales.order.mapper;

import com.dtp.cosmemgt.catalog.mapper.ProductVariantMapper;
import com.dtp.cosmemgt.sales.order.dto.response.OrderDetailItemsResponse;
import com.dtp.cosmemgt.sales.order.entity.OrderDetail;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring",
        builder = @Builder(disableBuilder = true))
public interface OrderDetailMapper {
    OrderDetailItemsResponse toOrderDetailItemsResponse (OrderDetail orderDetail);
}