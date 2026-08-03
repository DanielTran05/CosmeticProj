package com.dtp.cosmemgt.catalog.internal.controller;

import com.dtp.cosmemgt.catalog.internal.dto.request.CategoryCreationRequest;
import com.dtp.cosmemgt.catalog.internal.dto.response.AdminCategoryResponse;
import com.dtp.cosmemgt.catalog.internal.service.AdminCategoryService;
import com.dtp.cosmemgt.core.dto.ApiResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AdminCategoryController {
    AdminCategoryService categoryService;

    @PostMapping()
    ApiResponse<AdminCategoryResponse> create(@RequestBody CategoryCreationRequest request) {
        return ApiResponse.<AdminCategoryResponse>builder()
                .result(categoryService.create(request))
                .build();
    }

    @GetMapping()
    ApiResponse<List<AdminCategoryResponse>> getAll() {
        return ApiResponse.<List<AdminCategoryResponse>>builder()
                .result(categoryService.getAll())
                .build();
    }

    @GetMapping("/{categoryId}")
    ApiResponse<AdminCategoryResponse> getCateById(@PathVariable int categoryId) {
        return ApiResponse.<AdminCategoryResponse>builder()
                .result(categoryService.getCateById(categoryId))
                .build();
    }

    @PutMapping("/{categoryId}")
    ApiResponse<AdminCategoryResponse> updateCate(@PathVariable int categoryId,
                                                  @RequestBody CategoryCreationRequest request) {
        return ApiResponse.<AdminCategoryResponse>builder()
                .result(categoryService.update(categoryId, request))
                .build();
    }

    @DeleteMapping("/soft_del/{categoryId}")
    ApiResponse<Void> softDeleteCate(@PathVariable int categoryId) {
        categoryService.sftDelCate(categoryId);
        return ApiResponse.<Void>builder()
                .message("Category with id " + categoryId + " has been soft deleted successfully.")
                .build();
    }

    @PutMapping("/restore/{categoryId}")
    ApiResponse<Void> restoreCate(@PathVariable int categoryId) {
        categoryService.restoreCate(categoryId);
        return ApiResponse.<Void>builder()
                .message("Category with id " + categoryId + " has been restored successfully.")
                .build();
    }

    @DeleteMapping("/hard_del/{categoryId}")
    ApiResponse<Void> hardDelCate(@PathVariable int categoryId) {
        categoryService.hardDelCate(categoryId);
        return ApiResponse.<Void>builder()
                .message("Category with id " + categoryId + " has been hard deleted successfully.")
                .build();
    }
}