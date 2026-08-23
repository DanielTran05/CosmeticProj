package com.dtp.cosmemgt.catalog.controller.admin;

import com.dtp.cosmemgt.catalog.dto.request.CategoryCreationRequest;
import com.dtp.cosmemgt.catalog.dto.request.CategoryUpdateRequest;
import com.dtp.cosmemgt.catalog.dto.response.CategoryResponse;
import com.dtp.cosmemgt.catalog.service.CategoryService;
import com.dtp.cosmemgt.core.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("admin/categories")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AdminCategoryController {
    CategoryService categoryService;

    @PostMapping()
    ApiResponse<CategoryResponse> create(@RequestBody @Valid CategoryCreationRequest request) {
        return ApiResponse.<CategoryResponse>builder()
                .result(categoryService.create(request))
                .build();
    }

    @GetMapping()
    ApiResponse<List<CategoryResponse>> getAll() {
        return ApiResponse.<List<CategoryResponse>>builder()
                .result(categoryService.getAll())
                .build();
    }

    @GetMapping("/{categoryId}")
    ApiResponse<CategoryResponse> getCateById(@PathVariable int categoryId) {
        return ApiResponse.<CategoryResponse>builder()
                .result(categoryService.getCateById(categoryId))
                .build();
    }

    @PutMapping("/{categoryId}")
    ApiResponse<CategoryResponse> updateCate(@PathVariable int categoryId,
                                             @RequestBody @Valid CategoryUpdateRequest request) {
        return ApiResponse.<CategoryResponse>builder()
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