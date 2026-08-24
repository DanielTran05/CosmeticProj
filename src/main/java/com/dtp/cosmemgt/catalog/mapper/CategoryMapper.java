package com.dtp.cosmemgt.catalog.mapper;

import com.dtp.cosmemgt.catalog.dto.request.CategoryCreationRequest;
import com.dtp.cosmemgt.catalog.dto.response.CategoryResponse;
import com.dtp.cosmemgt.catalog.dto.response.CustomerCategoryResponse;
import com.dtp.cosmemgt.catalog.entity.Category;
import com.dtp.cosmemgt.core.coreMapper.IgnoreAuditFields;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring",
        builder = @org.mapstruct.Builder(disableBuilder = true))
public interface CategoryMapper {
    @IgnoreAuditFields
    @Mapping(target = "parentCategory", ignore = true)
    Category toCategory(CategoryCreationRequest request);

    CategoryResponse toCategoryResponse(Category category);
    CustomerCategoryResponse toCustomerCategoryResponse(Category category);
}
