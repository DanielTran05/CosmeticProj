package com.dtp.cosmemgt.catalog.service.command;

import com.dtp.cosmemgt.catalog.entity.Product;
import com.dtp.cosmemgt.catalog.entity.ProductVariant;
import com.dtp.cosmemgt.catalog.repository.ProductRepository;
import com.dtp.cosmemgt.catalog.repository.ProductVariantRepository;
import com.dtp.cosmemgt.core.exception.AppException;
import com.dtp.cosmemgt.core.exception.ErrorCode;
import com.dtp.cosmemgt.sales.promotion.dto.request.TargetItemRequest;
import com.dtp.cosmemgt.sales.promotion.entity.Promotion;
import com.dtp.cosmemgt.sales.promotion.entity.PromotionTargetItem;
import com.dtp.cosmemgt.sales.promotion.enums.TargetType;
import com.dtp.cosmemgt.sales.promotion.repository.PromotionRepository;
import com.dtp.cosmemgt.sales.promotion.service.PromotionCalculator;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ProductSyncService {

    ProductRepository productRepository;
    PromotionRepository promotionRepository;
    PromotionCalculator promotionCalculator;
    ProductVariantRepository productVariantRepository;

    @Transactional
    public void syncProductPricing(String productId) {
        Product product = productRepository.findByIdWithVariants(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXISTED));

        if (product.getProductVariants().isEmpty()) {
            product.setMinPrice(null);
            product.setMinDiscountedPrice(null);
            product.setRepresentativeVariantId(null);
            productRepository.save(product);
            return;
        }

        List<String> variantIds = product.getProductVariants().stream()
                .map(ProductVariant::getId)
                .toList();

        List<Promotion> activePromotions = promotionRepository.findActiveProductPromotions(
                productId,
                variantIds,
                LocalDateTime.now()
        );

        BigDecimal minOriginal = null;
        BigDecimal minDiscount = null;
        String repVariantId = null;
        String repVariantImg = null;

        for (ProductVariant variant : product.getProductVariants()) {
            BigDecimal original = variant.getUnitPrice();
            BigDecimal lowestDiscountedForThisVariant = original;

            for (Promotion promo : activePromotions) {
                boolean isApplicable = promo.getTargetItems().stream()
                        .map(PromotionTargetItem::getTargetId)
                        .anyMatch(targetId -> targetId.equals(productId) || targetId.equals(variant.getId()));

                if (isApplicable) {
                    BigDecimal calculatedPrice = promotionCalculator.calculateDiscountedPrice(original, promo);
                    if (calculatedPrice.compareTo(lowestDiscountedForThisVariant) < 0) {
                        lowestDiscountedForThisVariant = calculatedPrice;
                    }
                }
            }

            variant.setDiscountedPrice(lowestDiscountedForThisVariant);

            if (minDiscount == null || lowestDiscountedForThisVariant.compareTo(minDiscount) < 0) {
                minDiscount = lowestDiscountedForThisVariant;
                minOriginal = original;
                repVariantId = variant.getId();
                repVariantImg = variant.getImg();
            }
        }

        productVariantRepository.saveAll(product.getProductVariants());

        product.setMinPrice(minOriginal);
        product.setMinDiscountedPrice(minDiscount);
        product.setRepresentativeVariantId(repVariantId);
        product.setRepresentativeVariantImg(repVariantImg);

        productRepository.save(product);
    }
}