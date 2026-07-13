package com.dtp.cosmemgt.warehouse.mapper;

import com.dtp.cosmemgt.core.coreMapper.IgnoreAuditFields;
import com.dtp.cosmemgt.warehouse.dto.request.SupplierCreationRequest;
import com.dtp.cosmemgt.warehouse.dto.response.SupplierResponse;
import com.dtp.cosmemgt.warehouse.entity.Supplier;
import org.mapstruct.*;

@Mapper(componentModel = "spring",
        builder = @org.mapstruct.Builder(disableBuilder = true))
public interface SupplierMapper {
    @IgnoreAuditFields
    Supplier toSupplier(SupplierCreationRequest request);

    SupplierResponse toSupplierResponse(Supplier supplier);

    @IgnoreAuditFields
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateSupplierFromRequest(SupplierCreationRequest request, @MappingTarget Supplier supplier);
}