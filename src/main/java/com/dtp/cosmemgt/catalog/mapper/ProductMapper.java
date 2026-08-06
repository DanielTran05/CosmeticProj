package com.dtp.cosmemgt.catalog.mapper;

import com.dtp.cosmemgt.catalog.dto.response.ProductDetailResponse;
import com.dtp.cosmemgt.catalog.dto.response.ProductResponse;
import com.dtp.cosmemgt.catalog.entity.Product;
import org.mapstruct.*;

@Mapper(componentModel = "spring",
        builder = @Builder(disableBuilder = true))
public interface ProductMapper {
    @Mapping(target = "id", source = "id")
    ProductResponse toProductResponse(Product product);

    @Mapping(target = "productVariantResponses", source = "productVariants")
    ProductDetailResponse toProductDetailResponse(Product product);
}
