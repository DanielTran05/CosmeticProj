package com.dtp.cosmemgt.sales.promotion.controller;

import com.dtp.cosmemgt.core.dto.ApiResponse;
import com.dtp.cosmemgt.sales.promotion.dto.response.PromotionResponse;
import com.dtp.cosmemgt.sales.promotion.service.query.PromotionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/promotions")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PromotionController {
    PromotionService promotionService;

    @GetMapping("/order-vouchers")
    ApiResponse<List<PromotionResponse>> getOrderVouchers() {
        return ApiResponse.<List<PromotionResponse>>builder()
                .result(promotionService.getOrderVouchers())
                .build();
    }
}
