package com.dtp.cosmemgt.warehouse.mapper;

import com.dtp.cosmemgt.admin.mapper.UserMapper;
import com.dtp.cosmemgt.sales.order.mapper.OrderDetailMapper;
import com.dtp.cosmemgt.sales.order.mapper.OrderMapper;
import com.dtp.cosmemgt.sales.order.entity.Order;
import com.dtp.cosmemgt.sales.review.dto.response.WarehouseOrderResponse;
import com.dtp.cosmemgt.sales.order.mapper.OrderShippingMapper;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring",
        builder = @Builder(disableBuilder = true),
        uses = {OrderShippingMapper.class, UserMapper.class, OrderMapper.class, OrderDetailMapper.class})
public interface WarehouseOrderMapper {
    @Mapping(target = "orderDetails", source = "order", qualifiedByName = "toOrderDetailResponse")
    WarehouseOrderResponse toWarehouseOrderResponse(Order order);
}
