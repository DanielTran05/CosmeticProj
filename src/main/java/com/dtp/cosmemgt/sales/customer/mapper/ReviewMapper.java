package com.dtp.cosmemgt.sales.customer.mapper;

import com.dtp.cosmemgt.sales.customer.dto.request.ReviewCreationRequest;
import com.dtp.cosmemgt.sales.customer.dto.request.ReviewUpdateRequest;
import com.dtp.cosmemgt.sales.customer.dto.response.ReviewResponse;
import com.dtp.cosmemgt.catalog.entity.ProductVariant;
import com.dtp.cosmemgt.core.coreMapper.IgnoreAuditFields;
import com.dtp.cosmemgt.sales.entity.Review;
import org.mapstruct.*;

@Mapper(componentModel = "spring",
        builder = @Builder(disableBuilder = true))
public interface ReviewMapper {
    @Mapping(target = "productVariant", ignore = true)
    @Mapping(target = "customer", ignore = true)
    Review toReview(ReviewCreationRequest request);

    @Mapping(source = "productVariant.id", target = "productVariantId")
    ReviewResponse toReviewResponse(Review review);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateReviewFromRequest(ReviewUpdateRequest request, @MappingTarget Review review);
}