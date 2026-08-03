package com.dtp.cosmemgt.sales.customer.mapper;

import com.dtp.cosmemgt.sales.customer.dto.response.OrderShippingResponse;
import com.dtp.cosmemgt.sales.entity.OrderShipping;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring",
        builder = @Builder(disableBuilder = true))
public interface OrderShippingMapper {
    OrderShippingResponse toOrderShippingResponse(OrderShipping orderShipping);
}
