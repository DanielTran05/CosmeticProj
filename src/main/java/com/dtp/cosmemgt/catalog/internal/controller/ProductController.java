package com.dtp.cosmemgt.catalog.internal.controller;

import com.dtp.cosmemgt.catalog.internal.dto.request.ProductCreationRequest;
import com.dtp.cosmemgt.catalog.internal.dto.response.ProductResponse;
import com.dtp.cosmemgt.catalog.internal.service.ProductService;
import com.dtp.cosmemgt.core.dto.ApiResponse;
import com.dtp.cosmemgt.core.dto.PageResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ProductController {
    ProductService productService;

    @PostMapping()
    ApiResponse<ProductResponse> create(@RequestBody ProductCreationRequest request) {
        return ApiResponse.<ProductResponse>builder()
                .result(productService.create(request))
                .build();
    }

    @GetMapping
    public ApiResponse<PageResponse<ProductResponse>> getAllProducts(@RequestParam Map<String, String> queryParams) {
        return ApiResponse.<PageResponse<ProductResponse>>builder()
                .result(productService.getAll(queryParams))
                .build();
    }

    @GetMapping("/{productId}")
    ApiResponse<ProductResponse> getProductById(@PathVariable String productId) {
        return ApiResponse.<ProductResponse>builder()
                .result(productService.getProductById(productId))
                .build();
    }

    @PutMapping("/{productId}")
    ApiResponse<ProductResponse> updateProduct(@PathVariable String productId,
                                             @RequestBody ProductCreationRequest request) {
        return ApiResponse.<ProductResponse>builder()
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
}