package com.dtp.cosmemgt.warehouse.mapper;

import com.dtp.cosmemgt.core.coreMapper.IgnoreAuditFields;
import com.dtp.cosmemgt.sales.customer.dto.response.OrderResponse;
import com.dtp.cosmemgt.sales.entity.Order;
import com.dtp.cosmemgt.warehouse.dto.request.SupplierCreationRequest;
import com.dtp.cosmemgt.warehouse.dto.response.SupplierResponse;
import com.dtp.cosmemgt.warehouse.entity.Supplier;
import org.mapstruct.*;

@Mapper(componentModel = "spring",
        builder = @Builder(disableBuilder = true))
public interface OrderMapper {
    OrderResponse toOrderResponse(Order oder);

    @IgnoreAuditFields
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateSupplierFromRequest(SupplierCreationRequest request, @MappingTarget Supplier supplier);
}