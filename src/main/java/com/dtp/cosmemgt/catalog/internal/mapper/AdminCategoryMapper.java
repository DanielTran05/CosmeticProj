package com.dtp.cosmemgt.catalog.internal.mapper;

import com.dtp.cosmemgt.catalog.internal.dto.request.CategoryCreationRequest;
import com.dtp.cosmemgt.catalog.internal.dto.response.AdminCategoryResponse;
import com.dtp.cosmemgt.catalog.entity.Category;
import com.dtp.cosmemgt.core.coreMapper.IgnoreAuditFields;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring",
        builder = @org.mapstruct.Builder(disableBuilder = true))
public interface AdminCategoryMapper {
    @IgnoreAuditFields
    @Mapping(target = "parentCategory", ignore = true)
    Category toCategory(CategoryCreationRequest request);

    AdminCategoryResponse toCategoryResponse(Category category);
}
