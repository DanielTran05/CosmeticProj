package com.dtp.cosmemgt.catalog.mapper;

import com.dtp.cosmemgt.catalog.entity.UnitOfMeasure;
import com.dtp.cosmemgt.catalog.dto.request.UomCreationRequest;
import com.dtp.cosmemgt.catalog.dto.response.UomResponse;
import com.dtp.cosmemgt.core.coreMapper.IgnoreAuditFields;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring",
        builder = @org.mapstruct.Builder(disableBuilder = true))
public interface UomMapper {
    @IgnoreAuditFields
    UnitOfMeasure toUom(UomCreationRequest request);

    UomResponse toUomResponse(UnitOfMeasure unitOfMeasure);
}
