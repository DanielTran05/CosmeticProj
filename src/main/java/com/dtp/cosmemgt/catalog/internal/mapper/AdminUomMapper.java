package com.dtp.cosmemgt.catalog.internal.mapper;

import com.dtp.cosmemgt.catalog.entity.UnitOfMeasure;
import com.dtp.cosmemgt.catalog.internal.dto.request.UomCreationRequest;
import com.dtp.cosmemgt.catalog.internal.dto.response.UomResponse;
import com.dtp.cosmemgt.core.coreMapper.IgnoreAuditFields;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring",
        builder = @org.mapstruct.Builder(disableBuilder = true))
public interface AdminUomMapper {
    @IgnoreAuditFields
    UnitOfMeasure toUom(UomCreationRequest request);

    UomResponse toUomResponse(UnitOfMeasure unitOfMeasure);
}
