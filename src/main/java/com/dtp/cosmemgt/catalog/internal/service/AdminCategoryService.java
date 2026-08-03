package com.dtp.cosmemgt.catalog.internal.service;

import com.dtp.cosmemgt.catalog.internal.dto.request.CategoryCreationRequest;
import com.dtp.cosmemgt.catalog.internal.dto.response.AdminCategoryResponse;
import com.dtp.cosmemgt.catalog.entity.Category;
import com.dtp.cosmemgt.catalog.internal.mapper.AdminCategoryMapper;
import com.dtp.cosmemgt.catalog.repository.CategoryRepository;
import com.dtp.cosmemgt.core.exception.AppException;
import com.dtp.cosmemgt.core.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
@Slf4j
public class AdminCategoryService {
    CategoryRepository categoryRepository;
    AdminCategoryMapper categoryMapper;

    public AdminCategoryResponse create(CategoryCreationRequest request) {
        Category c = categoryMapper.toCategory(request);

        if (categoryRepository.existsByName(c.getName())) {
            throw new AppException(ErrorCode.CATEGORY_EXISTED);
        }

        if (request.getParentId() != null) {
            Category parentCategory = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_EXISTED));
            c.setParentCategory(parentCategory);
        }

        return categoryMapper.toCategoryResponse(categoryRepository.save(c));
    }

    public AdminCategoryResponse getCateById(int categoryId){
        Category c = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_EXISTED));
        return categoryMapper.toCategoryResponse(c);
    }

    public List<AdminCategoryResponse> getAll(){
        List<Category> categories = categoryRepository.findAll();
        return categories.stream()
                .map(categoryMapper::toCategoryResponse)
                .toList();
    }

    public AdminCategoryResponse update(int categoryId, CategoryCreationRequest request){
        Category c = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_EXISTED));
        c.setName(request.getName());

        if(request.getParentId()!=null) {
            Category parentCategory = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_EXISTED));
            c.setParentCategory(parentCategory);
        }else{
            c.setParentCategory(null);
        }

        return categoryMapper.toCategoryResponse(categoryRepository.save(c));
    }

    public void sftDelCate(int categoryId) {
        Category c = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_EXISTED));
        categoryRepository.delete(c);
    }

    public void restoreCate(int categoryId) {
        int rowsAffected = categoryRepository.restoreById(categoryId);
        if (rowsAffected == 0) {
            throw new AppException(ErrorCode.CATEGORY_NOT_EXISTED);
        }
    }

    public void hardDelCate(int categoryId) {
        Category c = categoryRepository.getById(categoryId)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_EXISTED));
        categoryRepository.hardDelById(c.getId());
    }
}
