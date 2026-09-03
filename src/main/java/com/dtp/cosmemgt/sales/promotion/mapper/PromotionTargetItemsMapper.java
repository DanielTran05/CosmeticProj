package com.dtp.cosmemgt.sales.promotion.mapper;

import com.dtp.cosmemgt.sales.promotion.dto.response.PromotionTargetItemsResponse;
import com.dtp.cosmemgt.sales.promotion.entity.PromotionTargetItem;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring",
        builder = @Builder(disableBuilder = true))
public interface PromotionTargetItemsMapper {
    PromotionTargetItemsResponse toPromotionTargetItemsResponse(PromotionTargetItem promotionTargetItem);
}
