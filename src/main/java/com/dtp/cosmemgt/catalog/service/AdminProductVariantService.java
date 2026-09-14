package com.dtp.cosmemgt.catalog.service;

import com.dtp.cosmemgt.catalog.dto.request.ProductVariantUpdateRequest;
import com.dtp.cosmemgt.catalog.dto.response.SimpleVariantResponse;
import com.dtp.cosmemgt.catalog.entity.Product;
import com.dtp.cosmemgt.catalog.entity.ProductVariant;
import com.dtp.cosmemgt.catalog.entity.UnitOfMeasure;
import com.dtp.cosmemgt.catalog.dto.request.ProductVariantCreationRequest;
import com.dtp.cosmemgt.catalog.dto.response.AdminProductVariantResponse;
import com.dtp.cosmemgt.catalog.mapper.ProductVariantMapper;
import com.dtp.cosmemgt.catalog.repository.ProductRepository;
import com.dtp.cosmemgt.catalog.repository.ProductVariantRepository;
import com.dtp.cosmemgt.catalog.repository.UomRepository;
import com.dtp.cosmemgt.catalog.service.command.ProductSyncService;
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
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
@Slf4j
public class AdminProductVariantService {
    ProductVariantRepository productVariantRepository;
    ProductRepository productRepository;
    UomRepository uomRepository;
    ProductVariantMapper productVariantMapper;
    ProductSyncService productSyncService;

    public AdminProductVariantResponse create(ProductVariantCreationRequest request) {
        //check
        Product p = productRepository.findById(request.getProduct())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXISTED));
        UnitOfMeasure u = uomRepository.findById(request.getUnitOfMeasure())
                .orElseThrow(() -> new AppException(ErrorCode.UOM_NOT_EXISTED));

        if (productVariantRepository.existsByVariantName(request.getVariantName())) {
            throw new AppException(ErrorCode.PRODUCT_VARIANT_EXISTED);
        }

        ProductVariant pv = productVariantMapper.toProductVariant(request);
        pv.setProduct(p);
        pv.setUnitOfMeasure(u);

        productSyncService.syncProductPricing(pv.getProduct().getId());

        return productVariantMapper.toAdminProductVariantResponse(productVariantRepository.save(pv));
    }

    public PageResponse<AdminProductVariantResponse> getAll(Map<String, String> queryParams) {
        int page = queryParams.containsKey("page") ? Integer.parseInt(queryParams.get("page")) : 0;
        int size = queryParams.containsKey("size") ? Integer.parseInt(queryParams.get("size")) : 10;
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<ProductVariant> pvs;

        if (queryParams.containsKey("kw") && !queryParams.get("kw").trim().isEmpty()) {
            String kw = queryParams.get("kw").trim();
            pvs = productVariantRepository.searchByKeyword(kw, pageable);
        } else {
            pvs = productVariantRepository.findAll(pageable);
        }

        Page<AdminProductVariantResponse> pvResponse = pvs.map(productVariantMapper::toAdminProductVariantResponse);
        return PageResponse.of(pvResponse);
    }

    public PageResponse<SimpleVariantResponse> warehouseGetAll(Map<String, String> queryParams) {
        int page = queryParams.containsKey("page") ? Integer.parseInt(queryParams.get("page")) : 0;
        int size = queryParams.containsKey("size") ? Integer.parseInt(queryParams.get("size")) : 10;
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<ProductVariant> pvs;

        if (queryParams.containsKey("kw") && !queryParams.get("kw").trim().isEmpty()) {
            String kw = queryParams.get("kw").trim();
            pvs = productVariantRepository.searchByKeyword(kw, pageable);
        } else {
            pvs = productVariantRepository.findAll(pageable);
        }

        Page<SimpleVariantResponse> pvResponse = pvs.map(productVariantMapper::toSimpleVariantResponse);
        return PageResponse.of(pvResponse);
    }

    public AdminProductVariantResponse getProductVariantById(String productVariantId) {
        ProductVariant pv = productVariantRepository.findById(productVariantId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_VARIANT_NOT_EXISTED));
        return productVariantMapper.toAdminProductVariantResponse(pv);
    }

    public AdminProductVariantResponse update(String productVariantId, ProductVariantUpdateRequest request){
        ProductVariant pv = productVariantRepository.findById(productVariantId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXISTED));

        productVariantMapper.updateProductVariantfromRequest(request, pv);

        if(request.getProduct() != null) {
            Product p = productRepository.findById(request.getProduct())
                    .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXISTED));
            pv.setProduct(p);
        }

        if(request.getUnitOfMeasure() != null) {
            UnitOfMeasure u = uomRepository.findById(request.getUnitOfMeasure())
                    .orElseThrow(() -> new AppException(ErrorCode.UOM_NOT_EXISTED));
            pv.setUnitOfMeasure(u);
        }

        productSyncService.syncProductPricing(pv.getProduct().getId());

        return productVariantMapper.toAdminProductVariantResponse(productVariantRepository.save(pv));
    }

    public void sftDelProductVariant(String productVariantId) {
        ProductVariant pv = productVariantRepository.findById(productVariantId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_VARIANT_NOT_EXISTED));

        String productId = pv.getProduct().getId();

        productVariantRepository.delete(pv);

        productVariantRepository.flush();

        productSyncService.syncProductPricing(productId);
    }

    public void restoreProductVariant(String productVariantId) {
        int rowsAffected = productVariantRepository.restoreById(productVariantId);
        if (rowsAffected == 0) {
            throw new AppException(ErrorCode.PRODUCT_VARIANT_NOT_EXISTED);
        }

        ProductVariant pv = productVariantRepository.findById(productVariantId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_VARIANT_NOT_EXISTED));

        productSyncService.syncProductPricing(pv.getProduct().getId());
    }

    public void hardDelProductVariant(String productVariantId) {
        ProductVariant pv = productVariantRepository.findByIdForAdmin(productVariantId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_VARIANT_NOT_EXISTED));

        String productId = pv.getProduct().getId();

        productVariantRepository.hardDelById(pv.getId());

        productSyncService.syncProductPricing(productId);
    }

    public void checkProductVariantSftDeleted(String variantId){
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_VARIANT_NOT_EXISTED));

        if (variant.getDeletedAt() != null || variant.getProduct().getDeletedAt() != null) {
            log.warn("[Order] Order failed, the variant has been deleted. Variant ID: {}", variantId);
            throw new AppException(ErrorCode.PRODUCT_UNAVAILABLE);
        }
    }
}