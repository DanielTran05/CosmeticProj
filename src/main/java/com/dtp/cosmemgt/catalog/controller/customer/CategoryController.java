package com.dtp.cosmemgt.catalog.controller.customer;

import com.dtp.cosmemgt.catalog.dto.response.CategoryResponse;
import com.dtp.cosmemgt.catalog.service.query.CategoryQueryService;
import com.dtp.cosmemgt.core.dto.ApiResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CategoryController {
    CategoryQueryService categoryQueryservice;

    @GetMapping()
    ApiResponse<List<CategoryResponse>> getAll() {
        return ApiResponse.<List<CategoryResponse>>builder()
                .result(categoryQueryservice.getAll())
                .build();
    }
}
