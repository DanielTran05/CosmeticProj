package com.dtp.cosmemgt.sales.promotion.utils;

import com.dtp.cosmemgt.catalog.entity.ProductVariant;
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

    @Transactional          //tim ra nhung obj bi anh huong
    public void syncByTargetItems(List<TargetItemRequest> items) {
        if (items == null || items.isEmpty()) return;

        Set<String> productIdsToSync = new HashSet<>();

        items.stream()
                .filter(item -> item.getTargetType() == TargetType.PRODUCT)
                .forEach(item -> productIdsToSync.add(item.getTargetId()));

        List<String> variantIds = items.stream()
                .filter(item -> item.getTargetType() == TargetType.VARIANT)
                .map(TargetItemRequest::getTargetId)
                .toList();

        if (!variantIds.isEmpty()) {
            List<ProductVariant> variants = productVariantRepository.findAllById(variantIds);
            variants.forEach(v -> productIdsToSync.add(v.getProduct().getId()));
        }

        for (String productId : productIdsToSync) {
            productSyncService.syncProductPricing(productId);
        }
    }
}
