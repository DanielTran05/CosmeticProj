package com.dtp.cosmemgt.catalog.service.query;

import com.dtp.cosmemgt.catalog.dto.response.CategoryResponse;
import com.dtp.cosmemgt.catalog.dto.response.CustomerCategoryResponse;
import com.dtp.cosmemgt.catalog.entity.Category;
import com.dtp.cosmemgt.catalog.mapper.CategoryMapper;
import com.dtp.cosmemgt.catalog.repository.CategoryRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
@Slf4j
public class CategoryQueryService {
    CategoryRepository categoryRepository;

    @Cacheable(value = "Categories", key = "'all'", sync = true)
    public List<CustomerCategoryResponse> getAll() {
        return categoryRepository.findAllCustomerCate();
    }
}