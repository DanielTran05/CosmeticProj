package com.dtp.cosmemgt.warehouse.mapper;

import com.dtp.cosmemgt.core.coreMapper.IgnoreAuditFields;
import com.dtp.cosmemgt.warehouse.dto.request.BatchCreationRequest;
import com.dtp.cosmemgt.warehouse.dto.response.BatchResponse;
import com.dtp.cosmemgt.warehouse.dto.response.InventoryTransactionResponse;
import com.dtp.cosmemgt.warehouse.entity.InventoryBatch;
import com.dtp.cosmemgt.warehouse.entity.InventoryTransaction;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", builder = @org.mapstruct.Builder(disableBuilder = true))
public interface InventoryTransactionMapper {
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(source = "transactionType", target = "transactionType")
    InventoryTransactionResponse toInventoryTransactionResponse(InventoryTransaction inventoryTransaction);
}
