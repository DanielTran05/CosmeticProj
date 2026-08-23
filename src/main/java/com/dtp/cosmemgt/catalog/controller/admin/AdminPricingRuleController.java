package com.dtp.cosmemgt.catalog.controller.admin;

import com.dtp.cosmemgt.catalog.dto.request.PricingRuleCreationRequest;
import com.dtp.cosmemgt.catalog.dto.response.AdminPricingRuleResponse;
import com.dtp.cosmemgt.catalog.service.AdminPricingRuleService;
import com.dtp.cosmemgt.core.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/pricingRules")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AdminPricingRuleController {
    AdminPricingRuleService pricingRuleService;

    @PostMapping()
    ApiResponse<AdminPricingRuleResponse> create(@RequestBody @Valid PricingRuleCreationRequest request) {
        return ApiResponse.<AdminPricingRuleResponse>builder()
                .result(pricingRuleService.create(request))
                .build();
    }

    @GetMapping()
    ApiResponse<List<AdminPricingRuleResponse>> getAll() {
        return ApiResponse.<List<AdminPricingRuleResponse>>builder()
                .result(pricingRuleService.getAll())
                .build();
    }

    @GetMapping("/{pricingRuleId}")
    ApiResponse<AdminPricingRuleResponse> getCateById(@PathVariable int pricingRuleId) {
        return ApiResponse.<AdminPricingRuleResponse>builder()
                .result(pricingRuleService.getPrcRById(pricingRuleId))
                .build();
    }

    @PutMapping("/{pricingRuleId}")
    ApiResponse<AdminPricingRuleResponse> updateCate(@PathVariable int pricingRuleId,
                                                     @RequestBody @Valid PricingRuleCreationRequest request) {
        return ApiResponse.<AdminPricingRuleResponse>builder()
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