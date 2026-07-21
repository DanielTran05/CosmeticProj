package com.dtp.cosmemgt.catalog.internal.service;

import com.dtp.cosmemgt.catalog.internal.dto.request.ProductCreationRequest;
import com.dtp.cosmemgt.catalog.internal.dto.response.ProductResponse;
import com.dtp.cosmemgt.catalog.internal.mapper.ProductMapper;
import com.dtp.cosmemgt.catalog.entity.Product;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
@Slf4j
public class ProductService {
    ProductRepository productRepository;
    ProductCoreService productCoreService;
    ProductMapper productMapper;

    public ProductResponse create(ProductCreationRequest request) {
        Product c = productMapper.toProduct(request);

        if (productRepository.existsByName(c.getName())) {
            throw new AppException(ErrorCode.PRODUCT_EXISTED);
        }

        return productMapper.toProductResponse(productRepository.save(c));
    }

    public ProductResponse getProductById(String productId){
        Product c = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXISTED));
        return productMapper.toProductResponse(c);
    }

    public PageResponse<ProductResponse> getAll(Map<String, String> queryParams) {
        Page<Product> rawProductPage = productCoreService.getAll(queryParams);

        Page<ProductResponse> dtoProductRes = rawProductPage.map(productMapper::toProductResponse);

        return PageResponse.of(dtoProductRes);
    }

    public ProductResponse update(String productId, ProductCreationRequest request){
        Product c = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXISTED));
        c.setName(request.getName());

        return productMapper.toProductResponse(productRepository.save(c));
    }

    public void sftDelProduct(String productId) {
        Product c = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXISTED));
        productRepository.delete(c);
    }

    public void restoreProduct(String productId) {
        int rowsAffected = productRepository.restoreById(productId);
        if (rowsAffected == 0) {
            throw new AppException(ErrorCode.PRODUCT_NOT_EXISTED);
        }
    }

    public void hardDelProduct(String productId) {
        Product c = productRepository.getProductById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXISTED));
        productRepository.hardDelById(c.getId());
    }
}