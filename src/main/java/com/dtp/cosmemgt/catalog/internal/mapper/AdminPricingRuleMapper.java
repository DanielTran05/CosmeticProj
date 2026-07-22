package com.dtp.cosmemgt.catalog.internal.mapper;

import com.dtp.cosmemgt.catalog.internal.dto.request.PricingRuleCreationRequest;
import com.dtp.cosmemgt.catalog.internal.dto.response.AdminPricingRuleResponse;
import com.dtp.cosmemgt.catalog.entity.PricingRule;
import com.dtp.cosmemgt.core.coreMapper.IgnoreAuditFields;
import org.mapstruct.*;

@Mapper(componentModel = "spring",
        builder = @org.mapstruct.Builder(disableBuilder = true))
public interface AdminPricingRuleMapper {
    @IgnoreAuditFields
    PricingRule toPricingRule(PricingRuleCreationRequest request);

    AdminPricingRuleResponse toPricingRuleResponse(PricingRule pricingRule);

    @IgnoreAuditFields
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updatePricingRuleFromRequest(PricingRuleCreationRequest request, @MappingTarget PricingRule pricingRule);
}
