package com.dtp.cosmemgt.sales.promotion.mapper;

import com.dtp.cosmemgt.core.coreMapper.IgnoreAuditFields;
import com.dtp.cosmemgt.sales.promotion.dto.request.PromotionCreationRequest;
import com.dtp.cosmemgt.sales.promotion.dto.request.PromotionUpdateRequest;
import com.dtp.cosmemgt.sales.promotion.dto.response.AdminPromotionDetailResponse;
import com.dtp.cosmemgt.sales.promotion.dto.response.AdminPromotionResponse;
import com.dtp.cosmemgt.sales.promotion.dto.response.PromotionResponse;
import com.dtp.cosmemgt.sales.promotion.entity.Promotion;
import org.mapstruct.*;

@Mapper(componentModel = "spring",
        builder = @Builder(disableBuilder = true),
        uses = {PromotionTargetItemsMapper.class})
public interface PromotionMapper {
    @IgnoreAuditFields
    @Mapping(target = "targetItems", ignore = true)
    Promotion toPromotion(PromotionCreationRequest request);

    AdminPromotionResponse toAdminPromotionResponse(Promotion promotion);

    AdminPromotionDetailResponse toAdminPromotionDetailResponse(Promotion promotion);

    PromotionResponse toPromotionResponse(Promotion promotion);

    @IgnoreAuditFields
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "targetItems", ignore = true)
    void updatePromotionfromRequest(PromotionUpdateRequest request, @MappingTarget Promotion promotion);
}
