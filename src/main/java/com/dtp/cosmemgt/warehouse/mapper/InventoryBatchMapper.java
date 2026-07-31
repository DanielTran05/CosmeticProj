package com.dtp.cosmemgt.warehouse.mapper;

import com.dtp.cosmemgt.core.coreMapper.IgnoreAuditFields;
import com.dtp.cosmemgt.warehouse.dto.request.BatchCreationRequest;
import com.dtp.cosmemgt.warehouse.dto.response.BatchResponse;
import com.dtp.cosmemgt.warehouse.entity.InventoryBatch;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", builder = @org.mapstruct.Builder(disableBuilder = true))
public interface InventoryBatchMapper {
    @IgnoreAuditFields
    @Mapping(target = "productVariant", ignore = true)
    InventoryBatch toInventoryBatch(BatchCreationRequest batchCreationRequest);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    BatchResponse toBatchResponse(InventoryBatch inventoryBatch);
}
