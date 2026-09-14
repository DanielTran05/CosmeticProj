package com.dtp.cosmemgt.sales.promotion.service;

import com.dtp.cosmemgt.catalog.entity.ProductVariant;
import com.dtp.cosmemgt.core.exception.AppException;
import com.dtp.cosmemgt.core.exception.ErrorCode;
import com.dtp.cosmemgt.sales.promotion.dto.request.TargetItemRequest;
import com.dtp.cosmemgt.sales.promotion.entity.Promotion;
import com.dtp.cosmemgt.sales.promotion.entity.PromotionTargetItem;
import com.dtp.cosmemgt.sales.promotion.repository.PromotionRepository;
import com.dtp.cosmemgt.sales.promotion.utils.TargetItemsUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
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
public class OrderPromotionUsageService {
    PromotionRepository promotionRepository;
    AdminPromotionService adminPromotionService;
    PromotionCalculator promotionCalculator;
    TargetItemsUtils syncByTargetItems;

    @Transactional
    public void deductProductPromotions(List<ProductVariant> purchasedVariants) {
        if (purchasedVariants == null || purchasedVariants.isEmpty()) return;

        List<String> productIds = purchasedVariants.stream()
                .map(v -> v.getProduct().getId())
                .distinct()
                .toList();
        List<String> variantIds = purchasedVariants.stream()
                .map(ProductVariant::getId)
                .toList();

        Set<String> appliedPromotionIds = new HashSet<>();

        for (String productId : productIds) {
            List<Promotion> activePromotions = promotionRepository.findActiveProductPromotions(
                    productId, variantIds, LocalDateTime.now()
            );

            for (ProductVariant variant : purchasedVariants) {
                if (!variant.getProduct().getId().equals(productId)
                        || variant.getDiscountedPrice() == null) continue;

                BigDecimal original = variant.getUnitPrice();
                BigDecimal currentDiscounted = variant.getDiscountedPrice();

                for (Promotion promo : activePromotions) {
                    boolean isApplicable = promo.getTargetItems().stream()
                            .map(PromotionTargetItem::getTargetId)
                            .anyMatch(targetId -> targetId.equals(productId)
                                    || targetId.equals(variant.getId()));

                    if (isApplicable) {
                        BigDecimal calculatedPrice = promotionCalculator.calculateDiscountedPrice(original, promo);
                        if (calculatedPrice.compareTo(currentDiscounted) == 0) {
                            appliedPromotionIds.add(promo.getId());
                            break; 
                        }
                    }
                }
            }
        }

        // deduct limit usage
        for (String promoId : appliedPromotionIds) {
            int updatedRows = promotionRepository.incrementUsedCount(promoId);
            
            if (updatedRows == 0) {
                throw new AppException(ErrorCode.PROMOTION_OUT_OF_USAGE);
            }

            Promotion promotion = promotionRepository.findById(promoId).orElse(null);
            if (promotion != null && promotion.getUsedCount().equals(promotion.getUsageLimit())) {
                log.info("Mã giảm giá {} đã hết lượt. Kích hoạt tự động gỡ giảm giá...", promotion.getCode());

                promotion.setIsActive(false);
                promotionRepository.save(promotion);

                if (promotion.getTargetItems() != null && !promotion.getTargetItems().isEmpty()) {
                    List<TargetItemRequest> targets = promotion.getTargetItems().stream()
                            .map(item -> new TargetItemRequest(item.getTargetId(), item.getTargetType()))
                            .toList();
                    syncByTargetItems.syncByTargetItems(targets);
                }
            }
        }
    }

    @Transactional
    public void deductOrderVoucher(String voucherCode) {
        Promotion promo = promotionRepository.findByCode(voucherCode)
                .orElseThrow(() -> new AppException(ErrorCode.PROMOTION_NOT_EXISTED));

        int updatedRows = promotionRepository.incrementUsedCount(promo.getId());
        if (updatedRows == 0) {
            throw new AppException(ErrorCode.PROMOTION_OUT_OF_USAGE);
        }

        Promotion updatedPromo = promotionRepository.findById(promo.getId()).orElse(null);

        if (updatedPromo != null && updatedPromo.getUsedCount().equals(updatedPromo.getUsageLimit())) {
            log.info("Mã Voucher Đơn hàng {} đã hết lượt. Chuyển isActive = false...", updatedPromo.getCode());

            updatedPromo.setIsActive(false);
            promotionRepository.save(updatedPromo);
        }
    }

    @Transactional
    public void restoreProductPromotions(List<ProductVariant> purchasedVariants) {
        if (purchasedVariants == null || purchasedVariants.isEmpty()) return;

        List<String> productIds = purchasedVariants.stream()
                .map(v -> v.getProduct().getId())
                .distinct()
                .toList();
        List<String> variantIds = purchasedVariants.stream()
                .map(ProductVariant::getId)
                .toList();

        Set<String> appliedPromotionIds = new HashSet<>();

        for (String productId : productIds) {
            List<Promotion> activePromotions = promotionRepository.findActiveProductPromotions(
                    productId, variantIds, LocalDateTime.now()
            );

            for (ProductVariant variant : purchasedVariants) {
                if (!variant.getProduct().getId().equals(productId) || variant.getDiscountedPrice() == null) continue;

                BigDecimal original = variant.getUnitPrice();
                BigDecimal currentDiscounted = variant.getDiscountedPrice();

                for (Promotion promo : activePromotions) {
                    boolean isApplicable = promo.getTargetItems().stream()
                            .map(PromotionTargetItem::getTargetId)
                            .anyMatch(targetId -> targetId.equals(productId) || targetId.equals(variant.getId()));

                    if (isApplicable) {
                        BigDecimal calculatedPrice = promotionCalculator.calculateDiscountedPrice(original, promo);
                        if (calculatedPrice.compareTo(currentDiscounted) == 0) {
                            appliedPromotionIds.add(promo.getId());
                            break;
                        }
                    }
                }
            }
        }

        for (String promoId : appliedPromotionIds) {
            Promotion promotion = promotionRepository.findById(promoId).orElse(null);
            if (promotion != null && promotion.getUsedCount() > 0) {
                promotion.setUsedCount(promotion.getUsedCount() - 1);
                if (!promotion.getIsActive()) {
                    promotion.setIsActive(true);
                }
                promotionRepository.save(promotion);
                log.info("Hoàn trả 1 lượt dùng cho mã: {}", promotion.getCode());
            }
        }
    }

    @Transactional
    public void restoreOrderVoucher(String voucherCode) {
        if (voucherCode == null || voucherCode.isBlank()) return;

        Promotion promo = promotionRepository.findByCode(voucherCode).orElse(null);
        if (promo != null && promo.getUsedCount() > 0) {
            promo.setUsedCount(promo.getUsedCount() - 1);
            if (!promo.getIsActive()) {
                promo.setIsActive(true);
            }
            promotionRepository.save(promo);
            log.info("Hoàn trả 1 lượt dùng cho Voucher đơn hàng: {}", voucherCode);
        }
    }
}