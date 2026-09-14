package com.dtp.cosmemgt.catalog.controller.customer;

import com.dtp.cosmemgt.catalog.dto.response.ProductCardListResponse;
import com.dtp.cosmemgt.catalog.dto.response.ProductCardResponse;
import com.dtp.cosmemgt.catalog.dto.response.ProductDetailResponse;
import com.dtp.cosmemgt.catalog.dto.response.ProductResponse;
import com.dtp.cosmemgt.catalog.service.query.ProductQueryService;
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
@RequestMapping("/products")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ProductController {
    ProductQueryService productQueryService;

    @GetMapping()
    public PageResponse<ProductResponse> getAllProducts(@RequestParam Map<String, String> queryParams) {
        return productQueryService.getAll(queryParams);
    }

    @GetMapping("/{slug}")
    public ApiResponse<ProductDetailResponse> getProductBySlug(@PathVariable String slug) {
        return ApiResponse.<ProductDetailResponse>builder()
                .result(productQueryService.getProductBySlug(slug))
                .build();
    }

    @GetMapping("/best-sellers")
    public ApiResponse<ProductCardListResponse> get12BestSellingProducts() {
        return ApiResponse.<ProductCardListResponse>builder()
                .result(productQueryService.getTop12BestSellers())
                .build();
    }

    @GetMapping("/sale")
    public ApiResponse<List<ProductCardResponse>> get12SaleProduct() {
        return ApiResponse.<List<ProductCardResponse>>builder()
                .result(productQueryService.getTop12SaleProducts().getItems())
                .build();
    }
}