package com.dtp.cosmemgt.catalog.mapper;

import com.dtp.cosmemgt.catalog.dto.request.ProductCreationRequest;
import com.dtp.cosmemgt.catalog.dto.response.AdminProductResponse;
import com.dtp.cosmemgt.catalog.entity.Product;
import com.dtp.cosmemgt.core.coreMapper.IgnoreAuditFields;
import org.mapstruct.*;

@Mapper(componentModel = "spring",
        builder = @Builder(disableBuilder = true))
public interface AdminProductMapper {
    @IgnoreAuditFields
    Product toProduct(ProductCreationRequest request);

    AdminProductResponse toProductResponse(Product product);

    @IgnoreAuditFields
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateProductFromRequest(ProductCreationRequest request, @MappingTarget Product product);
}
