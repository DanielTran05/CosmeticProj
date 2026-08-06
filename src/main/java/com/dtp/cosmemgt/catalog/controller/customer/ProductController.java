package com.dtp.cosmemgt.catalog.controller.customer;

import com.dtp.cosmemgt.catalog.dto.response.BestSellerResponse;
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

    @GetMapping("/{productId}")
    public ApiResponse<ProductDetailResponse> getProductById(@PathVariable String productId) {
        return ApiResponse.<ProductDetailResponse>builder()
                .result(productQueryService.getProductById(productId))
                .build();
    }

    @GetMapping("/best-sellers")
    public List<BestSellerResponse> get12BestSellingProducts() {
        return productQueryService.top12BestSellingProducts();
     }
}
