package com.dtp.cosmemgt.catalog.service.query;

import com.dtp.cosmemgt.catalog.dto.response.BestSellerResponse;
import com.dtp.cosmemgt.catalog.dto.response.ProductDetailResponse;
import com.dtp.cosmemgt.catalog.dto.response.ProductResponse;
import com.dtp.cosmemgt.catalog.entity.Product;
import com.dtp.cosmemgt.catalog.mapper.ProductMapper;
import com.dtp.cosmemgt.catalog.repository.ProductRepository;
import com.dtp.cosmemgt.core.dto.PageResponse;
import com.dtp.cosmemgt.core.exception.AppException;
import com.dtp.cosmemgt.core.exception.ErrorCode;
import com.dtp.cosmemgt.core.utils.SlugUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
@Slf4j
public class ProductQueryService {
    ProductCoreService productCoreService;
    ProductMapper productMapper;
    ProductRepository productRepository;

    public ProductDetailResponse getProductBySlug(String slug){
        Product p = productRepository.findBySlug(slug)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXISTED));

        if(p.getDeletedAt()!=null)
            throw new AppException(ErrorCode.PRODUCT_UNAVAILABLE);

        return productMapper.toProductDetailResponse(p);
    }

    public PageResponse<ProductResponse> getAll(Map<String, String> queryParams) {
        Page<Product> rawProductPage = productCoreService.getAll(queryParams, false);

        Page<ProductResponse> dtoProductRes = rawProductPage.map(productMapper::toProductResponse);

        return PageResponse.of(dtoProductRes);
    }

    public List<BestSellerResponse> top12BestSellingProducts() {
        Pageable topTwelve = PageRequest.of(0, 12);
        return productRepository.findBestSellingVariant(topTwelve);
    }
}