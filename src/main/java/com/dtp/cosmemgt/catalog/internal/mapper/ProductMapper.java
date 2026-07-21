package com.dtp.cosmemgt.catalog.internal.mapper;

import com.dtp.cosmemgt.catalog.internal.dto.request.ProductCreationRequest;
import com.dtp.cosmemgt.catalog.internal.dto.response.ProductResponse;
import com.dtp.cosmemgt.catalog.entity.Product;
import com.dtp.cosmemgt.core.coreMapper.IgnoreAuditFields;
import org.mapstruct.*;

@Mapper(componentModel = "spring",
        builder = @Builder(disableBuilder = true))
public interface ProductMapper {
    @IgnoreAuditFields
    Product toProduct(ProductCreationRequest request);

    ProductResponse toProductResponse(Product product);

    @IgnoreAuditFields
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateProductFromRequest(ProductCreationRequest request, @MappingTarget Product product);
}
