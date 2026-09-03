package com.dtp.cosmemgt.sales.promotion.service;

import com.dtp.cosmemgt.sales.promotion.entity.Promotion;
import com.dtp.cosmemgt.sales.promotion.enums.DiscountType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class PromotionCalculator {
    public BigDecimal calculateDiscountedPrice(BigDecimal originalPrice, Promotion promotion){
        if(promotion == null) return originalPrice;

        BigDecimal finalPrice = originalPrice;

        if (promotion.getDiscountType() == DiscountType.PERCENT) {
            BigDecimal discountAmt = originalPrice.multiply(promotion.getDiscountValue())
                    .divide(BigDecimal.valueOf(100));
            finalPrice = originalPrice.subtract(discountAmt);
        } else if (promotion.getDiscountType() == DiscountType.FIXED_AMOUNT) {
            finalPrice = originalPrice.subtract(promotion.getDiscountValue());
        }

        return finalPrice.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : finalPrice;
    }
}
