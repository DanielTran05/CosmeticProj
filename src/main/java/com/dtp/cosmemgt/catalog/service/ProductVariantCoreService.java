package com.dtp.cosmemgt.catalog.service;

import com.dtp.cosmemgt.catalog.mapper.ProductVariantMapper;
import com.dtp.cosmemgt.catalog.repository.ProductRepository;
import com.dtp.cosmemgt.catalog.repository.ProductVariantRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Transactional
@Service
public class ProductVariantCoreService {
    ProductVariantRepository productVariantRepository;
    ProductRepository productRepository;
    ProductVariantMapper productVariantMapper;

//    public List<ProductVariant> getAllVariantOfProduct(String productId) {
//        Product p = productRepository.findById(productId)
//                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXISTED));
//
//        List<ProductVariant> pvs = productVariantRepository.findAllByProductId(p.getId());
//        return pvs;
//    }

}
