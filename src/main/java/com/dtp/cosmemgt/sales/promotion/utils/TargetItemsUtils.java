package com.dtp.cosmemgt.sales.promotion.utils;

import com.dtp.cosmemgt.catalog.entity.ProductVariant;
import com.dtp.cosmemgt.catalog.repository.ProductRepository;
import com.dtp.cosmemgt.catalog.repository.ProductVariantRepository;
import com.dtp.cosmemgt.catalog.service.command.ProductSyncService;
import com.dtp.cosmemgt.sales.promotion.dto.request.TargetItemRequest;
import com.dtp.cosmemgt.sales.promotion.enums.TargetType;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TargetItemsUtils {

    ProductVariantRepository productVariantRepository;
    ProductSyncService productSyncService;
    private final ProductRepository productRepository;

    @Transactional          //find items impacted
    public void syncByTargetItems(List<TargetItemRequest> items) {
        if (items == null || items.isEmpty()) return;

        Set<String> productIdsToSync = new HashSet<>();

        // 1. Chỉ nhận targetId nếu nó thực sự tồn tại trong bảng Product
        List<String> rawProductIds = items.stream()
                .filter(item -> item.getTargetType() == TargetType.PRODUCT)
                .map(TargetItemRequest::getTargetId)
                .toList();

        if (!rawProductIds.isEmpty()) {
            productRepository.findAllById(rawProductIds)
                    .forEach(p -> productIdsToSync.add(p.getId()));
        }

        // 2. Với variant, lấy an toàn product liên kết
        List<String> variantIds = items.stream()
                .filter(item -> item.getTargetType() == TargetType.VARIANT)
                .map(TargetItemRequest::getTargetId)
                .toList();

        if (!variantIds.isEmpty()) {
            List<ProductVariant> variants = productVariantRepository.findAllById(variantIds);
            variants.forEach(v -> {
                if (v.getProduct() != null) {
                    productIdsToSync.add(v.getProduct().getId());
                }
            });
        }

        // 3. Tiến hành đồng bộ giá
        for (String productId : productIdsToSync) {
            productSyncService.syncProductPricing(productId);
        }
    }
}
