package com.dtp.cosmemgt.sales.internal.mapper;

import com.dtp.cosmemgt.sales.entity.OrderShipping;
import com.dtp.cosmemgt.sales.internal.dto.response.OrderShippingResponse;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring",
        builder = @Builder(disableBuilder = true))
public interface OrderShippingMapper {
    OrderShippingResponse toOrderShippingResponse(OrderShipping orderShipping);

}
