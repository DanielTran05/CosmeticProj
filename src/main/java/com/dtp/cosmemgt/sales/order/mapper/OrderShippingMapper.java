package com.dtp.cosmemgt.sales.order.mapper;

import com.dtp.cosmemgt.sales.order.dto.response.OrderShippingResponse;
import com.dtp.cosmemgt.sales.order.entity.OrderShipping;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring",
        builder = @Builder(disableBuilder = true))
public interface OrderShippingMapper {
    OrderShippingResponse toOrderShippingResponse(OrderShipping orderShipping);
}
