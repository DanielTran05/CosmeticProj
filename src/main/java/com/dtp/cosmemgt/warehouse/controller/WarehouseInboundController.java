package com.dtp.cosmemgt.warehouse.controller;

import com.dtp.cosmemgt.core.dto.ApiResponse;
import com.dtp.cosmemgt.core.dto.PageResponse;
import com.dtp.cosmemgt.warehouse.dto.request.BatchCreationRequest;
import com.dtp.cosmemgt.warehouse.dto.request.InventoryAdjustmentRequest;
import com.dtp.cosmemgt.warehouse.dto.response.BatchResponse;
import com.dtp.cosmemgt.warehouse.dto.response.InventoryTransactionResponse;
import com.dtp.cosmemgt.warehouse.dto.response.ProductBatchGroupResponse;
import com.dtp.cosmemgt.warehouse.entity.InventoryBatch;
import com.dtp.cosmemgt.warehouse.repository.InventoryBatchRepository;
import com.dtp.cosmemgt.warehouse.service.WarehouseInboundService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/warehouse/inbound")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WarehouseInboundController {

    WarehouseInboundService warehouseInboundService;
    private final InventoryBatchRepository inventoryBatchRepository;

    @PostMapping("/batches")
    public ApiResponse<BatchResponse> createBatch(@RequestBody @Valid BatchCreationRequest request) {
        return ApiResponse.<BatchResponse>builder()
                .result(warehouseInboundService.create(request))
                .build();
    }

    //get all batches
    @GetMapping("/batches")
    public ApiResponse<PageResponse<BatchResponse>> getAllBatches(@RequestParam Map<String, String> queryParams) {
        return ApiResponse.<PageResponse<BatchResponse>>builder()
                .result(warehouseInboundService.getAllBatches(queryParams))
                .build();
    }

    @GetMapping("/batches/grouped")
    public ApiResponse<PageResponse<ProductBatchGroupResponse>> getBatchesGroupedByProduct(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.<PageResponse<ProductBatchGroupResponse>>builder()
                .result(warehouseInboundService.getBatchesGroupedByProduct(page, size))
                .build();
    }

    //canh bao sap het han
    @GetMapping("/batches/expiring")
    public ApiResponse<PageResponse<BatchResponse>> getExpiringBatches(
            @RequestParam(defaultValue = "30") int daysThreshold,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.<PageResponse<BatchResponse>>builder()
                .result(warehouseInboundService.getExpiringBatches(daysThreshold, page, size))
                .build();
    }

    //lich su giao dich lo hang
    @GetMapping("/batches/{batchId}/transactions")
    public ApiResponse<PageResponse<InventoryTransactionResponse>> getBatchTransactions(
            @PathVariable String batchId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.<PageResponse<InventoryTransactionResponse>>builder()
                .result(warehouseInboundService.getBatchTransactions(batchId, page, size))
                .build();
    }

    // kiem ke dieu chinh lo hang thuc te
    @PostMapping("/batches/adjustments")
    public ApiResponse<BatchResponse> physicalInventoryCount(
            @RequestBody @Valid InventoryAdjustmentRequest request) {
        return ApiResponse.<BatchResponse>builder()
                .result(warehouseInboundService.physicalInventoryCount(request))
                .build();
    }
}