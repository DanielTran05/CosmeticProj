package com.dtp.cosmemgt.catalog.internal.mapper;

import com.dtp.cosmemgt.catalog.entity.ProductVariant;
import com.dtp.cosmemgt.catalog.internal.dto.request.ProductVariantCreationRequest;
import com.dtp.cosmemgt.catalog.internal.dto.response.AdminProductVariantResponse;
import com.dtp.cosmemgt.core.coreMapper.IgnoreAuditFields;
import org.mapstruct.*;

@Mapper(componentModel = "spring",
        builder = @org.mapstruct.Builder(disableBuilder = true))
public interface AdminProductVariantMapper {
    @IgnoreAuditFields
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "unitOfMeasure", ignore = true)
    ProductVariant toProductVariant(ProductVariantCreationRequest request);

    @Mapping(source = "product.id", target = "product")
    @Mapping(source = "unitOfMeasure.id", target = "unitOfMeasure")
    AdminProductVariantResponse toProductVariantResponse(ProductVariant productVariant);

    @IgnoreAuditFields
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "unitOfMeasure", ignore = true)
    void updateProductVariantFromRequest(ProductVariantCreationRequest request, @MappingTarget ProductVariant productVariant);
}