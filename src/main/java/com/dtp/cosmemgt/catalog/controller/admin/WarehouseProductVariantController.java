package com.dtp.cosmemgt.catalog.controller.admin;

import com.dtp.cosmemgt.catalog.dto.response.SimpleVariantResponse;
import com.dtp.cosmemgt.catalog.service.AdminProductVariantService;
import com.dtp.cosmemgt.core.dto.ApiResponse;
import com.dtp.cosmemgt.core.dto.PageResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/warehouse/product_variants")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class WarehouseProductVariantController {
    AdminProductVariantService productVariantService;

    @GetMapping("/simple")
    public ApiResponse<PageResponse<SimpleVariantResponse>> getSimpleVariants(@RequestParam Map<String, String> params) {
        return ApiResponse.<PageResponse<SimpleVariantResponse>>builder()
                .result(productVariantService.warehouseGetAll(params))
                .build();
    }
}
