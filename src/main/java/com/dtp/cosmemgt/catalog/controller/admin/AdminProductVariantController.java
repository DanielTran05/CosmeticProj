package com.dtp.cosmemgt.catalog.controller.admin;

import com.dtp.cosmemgt.catalog.dto.request.ProductVariantCreationRequest;
import com.dtp.cosmemgt.catalog.dto.request.ProductVariantUpdateRequest;
import com.dtp.cosmemgt.catalog.dto.response.AdminProductVariantResponse;
import com.dtp.cosmemgt.catalog.dto.response.SimpleVariantResponse;
import com.dtp.cosmemgt.catalog.service.AdminProductVariantService;
import com.dtp.cosmemgt.core.dto.ApiResponse;
import com.dtp.cosmemgt.core.dto.PageResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/product_variants")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AdminProductVariantController {
    AdminProductVariantService productVariantService;

    @PostMapping()
    ApiResponse<AdminProductVariantResponse> create(@RequestBody @Valid ProductVariantCreationRequest request) {
        return ApiResponse.<AdminProductVariantResponse>builder()
                .result(productVariantService.create(request))
                .build();
    }

    @GetMapping()
    ApiResponse<PageResponse<AdminProductVariantResponse>> getAll(@RequestParam Map<String, String> queryParams) {
        return ApiResponse.<PageResponse<AdminProductVariantResponse>>builder()
                .result(productVariantService.getAll(queryParams))
                .build();
    }

    @GetMapping("/simple")
    public ApiResponse<PageResponse<SimpleVariantResponse>> getSimpleVariants(@RequestParam Map<String, String> params) {
        return ApiResponse.<PageResponse<SimpleVariantResponse>>builder()
                .result(productVariantService.warehouseGetAll(params))
                .build();
    }

    @GetMapping("/{productVariantId}")
    ApiResponse<AdminProductVariantResponse> getProductVariantById(@PathVariable String productVariantId) {
        return ApiResponse.<AdminProductVariantResponse>builder()
                .result(productVariantService.getProductVariantById(productVariantId))
                .build();
    }

    @PutMapping("/{productVariantId}")
    ApiResponse<AdminProductVariantResponse> updateProductVariant(@PathVariable String productVariantId,
                                                                  @RequestBody @Valid ProductVariantUpdateRequest request) {
        return ApiResponse.<AdminProductVariantResponse>builder()
                .result(productVariantService.update(productVariantId, request))
                .build();
    }

    @DeleteMapping("/soft_del/{productVariantId}")
    ApiResponse<Void> softDeleteProductVariant(@PathVariable String productVariantId) {
        productVariantService.sftDelProductVariant(productVariantId);
        return ApiResponse.<Void>builder()
                .message("ProductVariant with id " + productVariantId + " has been soft deleted successfully.")
                .build();
    }

    @PutMapping("/restore/{productVariantId}")
    ApiResponse<Void> restoreProductVariant(@PathVariable String productVariantId) {
        productVariantService.restoreProductVariant(productVariantId);
        return ApiResponse.<Void>builder()
                .message("ProductVariant with id " + productVariantId + " has been restored successfully.")
                .build();
    }

    @DeleteMapping("/hard_del/{productVariantId}")
    ApiResponse<Void> hardDelProductVariant(@PathVariable String productVariantId) {
        productVariantService.hardDelProductVariant(productVariantId);
        return ApiResponse.<Void>builder()
                .message("ProductVariant with id " + productVariantId + " has been hard deleted successfully.")
                .build();
    }

}