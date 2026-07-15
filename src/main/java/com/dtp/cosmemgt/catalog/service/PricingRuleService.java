package com.dtp.cosmemgt.catalog.service;
import com.dtp.cosmemgt.catalog.dto.request.PricingRuleCreationRequest;
import com.dtp.cosmemgt.catalog.dto.response.PricingRuleResponse;
import com.dtp.cosmemgt.catalog.entity.PricingRule;
import com.dtp.cosmemgt.catalog.mapper.PricingRuleMapper;
import com.dtp.cosmemgt.catalog.repository.PricingRuleRepository;
import com.dtp.cosmemgt.core.exception.AppException;
import com.dtp.cosmemgt.core.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
@Slf4j
public class PricingRuleService {
    PricingRuleRepository pricingRuleRepo;
    PricingRuleMapper pricingRuleMapper;

    public PricingRuleResponse create(PricingRuleCreationRequest request) {
        PricingRule p = pricingRuleMapper.toPricingRule(request);

        if (pricingRuleRepo.existsByName(p.getName())) {
            throw new AppException(ErrorCode.PRICING_RULE_EXISTED);
        }

        return pricingRuleMapper.toPricingRuleResponse(pricingRuleRepo.save(p));
    }

    public PricingRuleResponse getPrcRById(int pricingRuleId){
        PricingRule s = pricingRuleRepo.findById(pricingRuleId)
                .orElseThrow(() -> new AppException(ErrorCode.PRICING_RULE_NOT_EXISTED));
        return pricingRuleMapper.toPricingRuleResponse(s);
    }

    public List<PricingRuleResponse> getAll(){
        List<PricingRule> suppliers = pricingRuleRepo.findAll();
        return suppliers.stream()
                .map(pricingRuleMapper::toPricingRuleResponse)
                .toList();
    }

    public PricingRuleResponse update(int pricingRuleId, PricingRuleCreationRequest request){
        PricingRule s = pricingRuleRepo.findById(pricingRuleId)
                .orElseThrow(() -> new AppException(ErrorCode.PRICING_RULE_NOT_EXISTED));

        pricingRuleMapper.updatePricingRuleFromRequest(request, s);

        return pricingRuleMapper.toPricingRuleResponse(pricingRuleRepo.save(s));
    }

    public void sftDelPrcR(int pricingRuleId) {
        PricingRule s = pricingRuleRepo.findById(pricingRuleId)
                .orElseThrow(() -> new AppException(ErrorCode.PRICING_RULE_NOT_EXISTED));
        pricingRuleRepo.delete(s);
    }

    public void restorePrcR(int pricingRuleId) {
        int rowsAffected = pricingRuleRepo.restoreById(pricingRuleId);
        if (rowsAffected == 0) {
            throw new AppException(ErrorCode.PRICING_RULE_NOT_EXISTED);
        }
    }

    public void hardDelPrcR(int pricingRuleId) {
        PricingRule s = pricingRuleRepo.getById(pricingRuleId)
                .orElseThrow(() -> new AppException(ErrorCode.PRICING_RULE_NOT_EXISTED));
        pricingRuleRepo.hardDelById(s.getId());
    }
}
