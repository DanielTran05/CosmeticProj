package com.dtp.cosmemgt.catalog.controller;

import com.dtp.cosmemgt.catalog.dto.request.PricingRuleCreationRequest;
import com.dtp.cosmemgt.catalog.dto.response.PricingRuleResponse;
import com.dtp.cosmemgt.catalog.service.PricingRuleService;
import com.dtp.cosmemgt.core.dto.ApiResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pricingRules")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PricingRuleController {
    PricingRuleService pricingRuleService;

    @PostMapping()
    ApiResponse<PricingRuleResponse> create(@RequestBody PricingRuleCreationRequest request) {
        return ApiResponse.<PricingRuleResponse>builder()
                .result(pricingRuleService.create(request))
                .build();
    }

    @GetMapping()
    ApiResponse<List<PricingRuleResponse>> getAll() {
        return ApiResponse.<List<PricingRuleResponse>>builder()
                .result(pricingRuleService.getAll())
                .build();
    }

    @GetMapping("/{pricingRuleId}")
    ApiResponse<PricingRuleResponse> getCateById(@PathVariable int pricingRuleId) {
        return ApiResponse.<PricingRuleResponse>builder()
                .result(pricingRuleService.getPrcRById(pricingRuleId))
                .build();
    }

    @PutMapping("/{pricingRuleId}")
    ApiResponse<PricingRuleResponse> updateCate(@PathVariable int pricingRuleId,
                                             @RequestBody PricingRuleCreationRequest request) {
        return ApiResponse.<PricingRuleResponse>builder()
                .result(pricingRuleService.update(pricingRuleId, request))
                .build();
    }

    @DeleteMapping("/soft_del/{pricingRuleId}")
    ApiResponse<Void> softDeleteCate(@PathVariable int pricingRuleId) {
        pricingRuleService.sftDelPrcR(pricingRuleId);
        return ApiResponse.<Void>builder()
                .message("PricingRule with id " + pricingRuleId + " has been soft deleted successfully.")
                .build();
    }

    @PutMapping("/restore/{pricingRuleId}")
    ApiResponse<Void> restoreCate(@PathVariable int pricingRuleId) {
        pricingRuleService.restorePrcR(pricingRuleId);
        return ApiResponse.<Void>builder()
                .message("PricingRule with id " + pricingRuleId + " has been restored successfully.")
                .build();
    }

    @DeleteMapping("/hard_del/{pricingRuleId}")
    ApiResponse<Void> hardDelCate(@PathVariable int pricingRuleId) {
        pricingRuleService.hardDelPrcR(pricingRuleId);
        return ApiResponse.<Void>builder()
                .message("PricingRule with id " + pricingRuleId + " has been hard deleted successfully.")
                .build();
    }
}