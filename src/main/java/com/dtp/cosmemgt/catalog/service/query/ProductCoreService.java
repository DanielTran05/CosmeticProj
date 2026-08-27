package com.dtp.cosmemgt.catalog.service.query;

import com.dtp.cosmemgt.catalog.entity.Product;
import com.dtp.cosmemgt.catalog.entity.ProductVariant;
import com.dtp.cosmemgt.catalog.repository.ProductRepository;
import com.dtp.cosmemgt.catalog.repository.ProductVariantRepository;
import com.dtp.cosmemgt.catalog.service.specification.ProductSpecification;
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
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Transactional
@Service
public class ProductCoreService {
    ProductRepository productRepository;

    public Page<Product> getAll(Map<String, String> queryParams, boolean isAdmin) {
        Specification<Product> spec;
        if(isAdmin) {
            spec = ProductSpecification.filterProductForAdmin(queryParams);
        } else {
            spec = ProductSpecification.filterProductForCustomer(queryParams);
        }

        int page = queryParams.containsKey("page") ? Integer.parseInt(queryParams.get("page")) : 0;
        int size = queryParams.containsKey("size") ? Integer.parseInt(queryParams.get("size")) : 10;

        Sort sort = Sort.by("createdAt").descending();

        if (queryParams.containsKey("sort") && queryParams.get("sort") != null && !queryParams.get("sort").isEmpty()) {
            String[] sortParams = queryParams.get("sort").split(",");

            String sortBy = sortParams[0].equals("price") ? "basePrice" : sortParams[0];
            String sortDir = sortParams.length > 1 ? sortParams[1] : "asc";

            if (sortDir.equalsIgnoreCase("desc")) {
                sort = Sort.by(sortBy).descending();
            } else {
                sort = Sort.by(sortBy).ascending();
            }
        }

        Pageable pageable = PageRequest.of(page, size, sort);

        return productRepository.findAll(spec, pageable);
    }

    public List<ProductVariant> getAllVariantOfProduct(String productId) {
       Product p = productRepository.findByIdWithVariants(productId)
               .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXISTED));

       return p.getProductVariants();
    }
}
