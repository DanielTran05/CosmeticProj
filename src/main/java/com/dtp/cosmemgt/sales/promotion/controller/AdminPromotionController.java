package com.dtp.cosmemgt.sales.promotion.controller;


import com.dtp.cosmemgt.core.dto.ApiResponse;
import com.dtp.cosmemgt.core.dto.PageResponse;
import com.dtp.cosmemgt.sales.promotion.dto.request.PromotionCreationRequest;
import com.dtp.cosmemgt.sales.promotion.dto.request.PromotionUpdateRequest;
import com.dtp.cosmemgt.sales.promotion.dto.response.AdminPromotionDetailResponse;
import com.dtp.cosmemgt.sales.promotion.dto.response.AdminPromotionResponse;
import com.dtp.cosmemgt.sales.promotion.service.AdminPromotionService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/promotions")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AdminPromotionController {
    
    AdminPromotionService promotionService;

    @PostMapping()
    ApiResponse<AdminPromotionResponse> create(@RequestBody @Valid PromotionCreationRequest request) {
        return ApiResponse.<AdminPromotionResponse>builder()
                .result(promotionService.create(request))
                .build();
    }

    @GetMapping
    public ApiResponse<PageResponse<AdminPromotionResponse>> getAllPromotions(@RequestParam Map<String, String> queryParams) {
        return ApiResponse.<PageResponse<AdminPromotionResponse>>builder()
                .result(promotionService.getAll(queryParams))
                .build();
    }

    @GetMapping("/{promotionId}")
    ApiResponse<AdminPromotionDetailResponse> getPromotionById(@PathVariable String promotionId) {
        return ApiResponse.<AdminPromotionDetailResponse>builder()
                .result(promotionService.getPromotionById(promotionId))
                .build();
    }

    @PutMapping("/{promotionId}")
    ApiResponse<AdminPromotionResponse> updatePromotion(@PathVariable String promotionId,
                                                        @RequestBody @Valid PromotionUpdateRequest request) {
        return ApiResponse.<AdminPromotionResponse>builder()
                .result(promotionService.update(promotionId, request))
                .build();
    }

    @DeleteMapping("/soft_del/{promotionId}")
    ApiResponse<Void> softDeletePromotion(@PathVariable String promotionId) {
        promotionService.softDeletePromotion(promotionId);
        return ApiResponse.<Void>builder()
                .message("Promotion with id " + promotionId + " has been soft deleted successfully.")
                .build();
    }

    @PutMapping("/restore/{promotionId}")
    ApiResponse<Void> restorePromotion(@PathVariable String promotionId) {
        promotionService.restorePromotion(promotionId);
        return ApiResponse.<Void>builder()
                .message("Promotion with id " + promotionId + " has been restored successfully.")
                .build();
    }

    @DeleteMapping("/hard_del/{promotionId}")
    ApiResponse<Void> hardDeletePromotion(@PathVariable String promotionId) {
        promotionService.hardDeletePromotion(promotionId);
        return ApiResponse.<Void>builder()
                .message("Promotion with id " + promotionId + " has been hard deleted successfully.")
                .build();
    }


}