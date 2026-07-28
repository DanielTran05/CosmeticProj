package com.dtp.cosmemgt.sales.internal.mapper;

import com.dtp.cosmemgt.admin.mapper.UserMapper;
import com.dtp.cosmemgt.sales.customer.mapper.OrderMapper;
import com.dtp.cosmemgt.sales.entity.Order;
import com.dtp.cosmemgt.sales.internal.dto.response.WarehouseOrderResponse;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring",
        builder = @Builder(disableBuilder = true),
        uses = {OrderShippingMapper.class, UserMapper.class, OrderMapper.class})
public interface WarehouseOrderMapper {
    WarehouseOrderResponse toWarehouseOrderResponse (Order order);
}
