package com.dtp.cosmemgt.sales.customer.mapper;

import com.dtp.cosmemgt.sales.customer.dto.response.OrderDetailItemsResponse;
import com.dtp.cosmemgt.sales.entity.OrderDetail;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring",
        builder = @Builder(disableBuilder = true))
public interface OrderDetailMapper {
    OrderDetailItemsResponse toOrderDetailItemsResponse (OrderDetail orderDetail);
}
