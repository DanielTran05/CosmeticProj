package com.dtp.cosmemgt.catalog.service;

import com.dtp.cosmemgt.catalog.dto.request.ProductUpdateRequest;
import com.dtp.cosmemgt.catalog.dto.response.AdminSimpleProductResponse;
import com.dtp.cosmemgt.catalog.entity.Category;
import com.dtp.cosmemgt.catalog.entity.ProductVariant;
import com.dtp.cosmemgt.catalog.dto.request.ProductCreationRequest;
import com.dtp.cosmemgt.catalog.dto.response.AdminProductResponse;
import com.dtp.cosmemgt.catalog.dto.response.AdminProductVariantResponse;
import com.dtp.cosmemgt.catalog.entity.Product;
import com.dtp.cosmemgt.catalog.mapper.AdminProductMapper;
import com.dtp.cosmemgt.catalog.mapper.ProductVariantMapper;
import com.dtp.cosmemgt.catalog.repository.CategoryRepository;
import com.dtp.cosmemgt.catalog.repository.ProductRepository;
import com.dtp.cosmemgt.catalog.repository.ProductVariantRepository;
import com.dtp.cosmemgt.catalog.service.query.ProductCoreService;
import com.dtp.cosmemgt.core.dto.PageResponse;
import com.dtp.cosmemgt.core.exception.AppException;
import com.dtp.cosmemgt.core.exception.ErrorCode;
import com.dtp.cosmemgt.core.utils.SlugUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
@Slf4j
public class AdminProductService {
    ProductRepository productRepository;
    ProductVariantRepository productVariantRepository;
    ProductCoreService productCoreService;
    ProductVariantMapper productVariantMapper;
    AdminProductMapper productMapper;
    CategoryRepository categoryRepository;

    public AdminProductResponse create(ProductCreationRequest request) {
        Product p = productMapper.toProduct(request);

        Category cate = categoryRepository.findById(request.getCateId())
                .orElseThrow(() ->new AppException(ErrorCode.CATEGORY_NOT_EXISTED));

        String baseSlug = SlugUtils.toSlug(request.getName());
        String finalSlug = baseSlug;

        if (productRepository.existsBySlug(baseSlug)) {
            String randomStr = java.util.UUID.randomUUID().toString().substring(0, 5);
            finalSlug = baseSlug + "-" + randomStr;
        }
        p.setSlug(finalSlug);

        if (productRepository.existsByName(p.getName())) {
            throw new AppException(ErrorCode.PRODUCT_EXISTED);
        }

        p.setCategory(cate);

        return productMapper.toProductResponse(productRepository.save(p));
    }

    public AdminProductResponse getProductById(String productId){
        Product c = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXISTED));
        return productMapper.toProductResponse(c);
    }

    public PageResponse<AdminProductResponse> getAll(Map<String, String> queryParams) {
        Page<Product> rawProductPage = productCoreService.getAll(queryParams, true);

        Page<AdminProductResponse> dtoProductRes = rawProductPage.map(productMapper::toProductResponse);

        return PageResponse.of(dtoProductRes);
    }

    public PageResponse<AdminSimpleProductResponse> getAllSimple(Map<String, String> queryParams) {
        Page<Product> rawProductPage = productCoreService.getAll(queryParams, true);

        Page<AdminSimpleProductResponse> dtoProductRes = rawProductPage.map(productMapper::toAdminSimpleProductResponse);

        return PageResponse.of(dtoProductRes);
    }

    public List<AdminProductVariantResponse> getAllVariantOfProduct(String productId) {
        List<ProductVariant> pvs = productCoreService.getAllVariantOfProduct(productId);

        return pvs.stream()
                .map(productVariantMapper::toAdminProductVariantResponse)
                .toList();
    }

    public AdminProductResponse update(String productId, ProductUpdateRequest request){
        Product p = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXISTED));

        Category cate = categoryRepository.findById(request.getCateId())
                .orElseThrow(() ->new AppException(ErrorCode.CATEGORY_NOT_EXISTED));

        productMapper.updateProductFromRequest(request, p);
        p.setCategory(cate);

        return productMapper.toProductResponse(productRepository.save(p));
    }

    public void sftDelProduct(String productId) {
        Product c = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXISTED));

        List<ProductVariant> pv = c.getProductVariants();
        productVariantRepository.deleteAll(pv);

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