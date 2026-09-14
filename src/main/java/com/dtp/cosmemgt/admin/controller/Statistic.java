package com.dtp.cosmemgt.admin.controller;

import com.cloudinary.Api;
import com.dtp.cosmemgt.admin.dto.response.MonthlyStatisticResponse;
import com.dtp.cosmemgt.admin.dto.response.OverviewStatisticResponse;
import com.dtp.cosmemgt.admin.service.StatisticService;
import com.dtp.cosmemgt.catalog.dto.response.CategoryShareResponse;
import com.dtp.cosmemgt.catalog.dto.response.InventoryValuationResponse;
import com.dtp.cosmemgt.catalog.dto.response.TopSellingVariantResponse;
import com.dtp.cosmemgt.core.dto.ApiResponse;
import com.dtp.cosmemgt.warehouse.dto.response.NearExpiryBatchResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/admin/statistics")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class Statistic {
    StatisticService statisticService;

    @GetMapping("/overview")
    public ApiResponse<OverviewStatisticResponse> getOverview(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        return ApiResponse.<OverviewStatisticResponse>builder()
                .result(statisticService.getOverview(startDate, endDate))
                .build();
    }

    @GetMapping("/charts/monthly")
    public ApiResponse<List<MonthlyStatisticResponse>> getMonthlyChart(
            @RequestParam(defaultValue = "2026") int year) {

        return ApiResponse.<List<MonthlyStatisticResponse>>builder()
                .result(statisticService.getMonthlyChart(year))
                .build();
    }

    @GetMapping("/top-sellers")
    public ApiResponse<List<TopSellingVariantResponse>> getTopSellers(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ApiResponse.<List<TopSellingVariantResponse>>builder()
                .result(statisticService.getTopSellingVariants(startDate, endDate))
                .build();
    }

    @GetMapping("/categories-share")
    public ApiResponse<List<CategoryShareResponse>> getCategoryShare(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ApiResponse.<List<CategoryShareResponse>>builder()
                .result(statisticService.getCategoryShare(startDate, endDate))
                .build();
    }

    @GetMapping("/inventory/valuation")
    public ApiResponse<InventoryValuationResponse> getInventoryValuation() {
        return ApiResponse.<InventoryValuationResponse>builder()
                .result(statisticService.getInventoryValuation())
                .build();
    }

    @GetMapping("/inventory/near-expiry")
    public ApiResponse<List<NearExpiryBatchResponse>> getNearExpiryBatches(
            @RequestParam(defaultValue = "30") int alertDays) {
        return ApiResponse.<List<NearExpiryBatchResponse>>builder()
                .result(statisticService.getNearExpiryBatches(alertDays))
                .build();
    }
}
