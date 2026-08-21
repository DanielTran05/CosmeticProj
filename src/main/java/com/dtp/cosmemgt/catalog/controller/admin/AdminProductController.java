package com.dtp.cosmemgt.catalog.controller.admin;

import com.dtp.cosmemgt.catalog.service.AdminProductVariantService;
import com.dtp.cosmemgt.catalog.dto.request.ProductCreationRequest;
import com.dtp.cosmemgt.catalog.dto.response.AdminProductResponse;
import com.dtp.cosmemgt.catalog.service.AdminProductService;
import com.dtp.cosmemgt.catalog.dto.response.AdminProductVariantResponse;
import com.dtp.cosmemgt.core.dto.ApiResponse;
import com.dtp.cosmemgt.core.dto.PageResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/products")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AdminProductController {
    AdminProductService productService;

    @PostMapping()
    ApiResponse<AdminProductResponse> create(@RequestBody ProductCreationRequest request) {
        return ApiResponse.<AdminProductResponse>builder()
                .result(productService.create(request))
                .build();
    }

    @GetMapping
    public ApiResponse<PageResponse<AdminProductResponse>> getAllProducts(@RequestParam Map<String, String> queryParams) {
        return ApiResponse.<PageResponse<AdminProductResponse>>builder()
                .result(productService.getAll(queryParams))
                .build();
    }

    @GetMapping("/{productId}")
    ApiResponse<AdminProductResponse> getProductById(@PathVariable String productId) {
        return ApiResponse.<AdminProductResponse>builder()
                .result(productService.getProductById(productId))
                .build();
    }

    @PutMapping("/{productId}")
    ApiResponse<AdminProductResponse> updateProduct(@PathVariable String productId,
                                                    @RequestBody ProductCreationRequest request) {
        return ApiResponse.<AdminProductResponse>builder()
                .result(productService.update(productId, request))
                .build();
    }

    @DeleteMapping("/soft_del/{productId}")
    ApiResponse<Void> softDeleteProduct(@PathVariable String productId) {
        productService.sftDelProduct(productId);
        return ApiResponse.<Void>builder()
                .message("Product with id " + productId + " has been soft deleted successfully.")
                .build();
    }

    @PutMapping("/restore/{productId}")
    ApiResponse<Void> restoreProduct(@PathVariable String productId) {
        productService.restoreProduct(productId);
        return ApiResponse.<Void>builder()
                .message("Product with id " + productId + " has been restored successfully.")
                .build();
    }

    @DeleteMapping("/hard_del/{productId}")
    ApiResponse<Void> hardDelProduct(@PathVariable String productId) {
        productService.hardDelProduct(productId);
        return ApiResponse.<Void>builder()
                .message("Product with id " + productId + " has been hard deleted successfully.")
                .build();
    }


    @GetMapping("/{productId}/variants")
    ApiResponse<List<AdminProductVariantResponse>> getAllVariantOfProduct(@PathVariable String productId) {
        return ApiResponse.<List<AdminProductVariantResponse>>builder()
                .result(productService.getAllVariantOfProduct(productId))
                .build();
    }
}