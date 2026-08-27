package com.dtp.cosmemgt.catalog.mapper;

import com.dtp.cosmemgt.catalog.dto.request.ProductVariantUpdateRequest;
import com.dtp.cosmemgt.catalog.dto.response.ProductVariantResponse;
import com.dtp.cosmemgt.catalog.dto.response.SimpleVariantResponse;
import com.dtp.cosmemgt.catalog.entity.ProductVariant;
import com.dtp.cosmemgt.catalog.dto.request.ProductVariantCreationRequest;
import com.dtp.cosmemgt.catalog.dto.response.AdminProductVariantResponse;
import com.dtp.cosmemgt.core.coreMapper.IgnoreAuditFields;
import org.mapstruct.*;

@Mapper(componentModel = "spring",
        builder = @org.mapstruct.Builder(disableBuilder = true))
public interface ProductVariantMapper {
    @IgnoreAuditFields
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "unitOfMeasure", ignore = true)
    ProductVariant toProductVariant(ProductVariantCreationRequest request);

    @Mapping(source = "product.id", target = "product")
    @Mapping(source = "unitOfMeasure.name", target = "unitOfMeasure")
    AdminProductVariantResponse toAdminProductVariantResponse(ProductVariant productVariant);

    @Mapping(source = "product.name", target = "productName")
    SimpleVariantResponse toSimpleVariantResponse(ProductVariant productVariant);

    @IgnoreAuditFields
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "unitOfMeasure", ignore = true)
    void updateProductVariantFromRequest(ProductVariantUpdateRequest request, @MappingTarget ProductVariant productVariant);



    //CUSTOMER
    @Mapping(target = "oum", source = "unitOfMeasure.name")
    ProductVariantResponse toProductVariantResponse(ProductVariant productVariant);

}