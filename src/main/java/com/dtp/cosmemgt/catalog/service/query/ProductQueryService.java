package com.dtp.cosmemgt.catalog.service.query;

import com.dtp.cosmemgt.catalog.dto.response.BestSellerResponse;
import com.dtp.cosmemgt.catalog.dto.response.ProductDetailResponse;
import com.dtp.cosmemgt.catalog.dto.response.ProductResponse;
import com.dtp.cosmemgt.catalog.entity.Product;
import com.dtp.cosmemgt.catalog.mapper.ProductMapper;
import com.dtp.cosmemgt.catalog.repository.ProductRepository;
import com.dtp.cosmemgt.catalog.service.ProductCoreService;
import com.dtp.cosmemgt.core.dto.PageResponse;
import com.dtp.cosmemgt.core.exception.AppException;
import com.dtp.cosmemgt.core.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
@Slf4j
public class ProductQueryService {
    ProductCoreService productCoreService;
    ProductMapper productMapper;
    ProductRepository productRepository;

    public ProductDetailResponse getProductById(String productId){
        Product c = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXISTED));
        return productMapper.toProductDetailResponse(c);
    }

    public PageResponse<ProductResponse> getAll(Map<String, String> queryParams) {
        Page<Product> rawProductPage = productCoreService.getAll(queryParams);

        Page<ProductResponse> dtoProductRes = rawProductPage.map(productMapper::toProductResponse);

        return PageResponse.of(dtoProductRes);
    }

    public List<BestSellerResponse> top12BestSellingProducts() {
        Pageable topTwelve = PageRequest.of(0, 12);
        return productRepository.findBestSellingVariant(topTwelve);
    }
}
