package com.dtp.cosmemgt.catalog.mapper;

import com.dtp.cosmemgt.catalog.dto.request.PricingRuleCreationRequest;
import com.dtp.cosmemgt.catalog.dto.response.PricingRuleResponse;
import com.dtp.cosmemgt.catalog.entity.PricingRule;
import com.dtp.cosmemgt.core.coreMapper.IgnoreAuditFields;
import org.mapstruct.*;

@Mapper(componentModel = "spring",
        builder = @org.mapstruct.Builder(disableBuilder = true))
public interface PricingRuleMapper {
    @IgnoreAuditFields
    PricingRule toPricingRule(PricingRuleCreationRequest request);

    PricingRuleResponse toPricingRuleResponse(PricingRule pricingRule);

    @IgnoreAuditFields
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updatePricingRuleFromRequest(PricingRuleCreationRequest request, @MappingTarget PricingRule pricingRule);
}
