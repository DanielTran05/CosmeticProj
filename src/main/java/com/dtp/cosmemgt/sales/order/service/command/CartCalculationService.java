package com.dtp.cosmemgt.sales.order.service.command;

import com.dtp.cosmemgt.catalog.entity.ProductVariant;
import com.dtp.cosmemgt.catalog.repository.ProductVariantRepository;
import com.dtp.cosmemgt.sales.order.dto.request.CartCalculateRequest;
import com.dtp.cosmemgt.sales.order.dto.request.OrderDetailRequest;
import com.dtp.cosmemgt.sales.order.dto.response.CartCalculateResponse;
import com.dtp.cosmemgt.sales.promotion.entity.Promotion;
import com.dtp.cosmemgt.sales.promotion.enums.ScopeType;
import com.dtp.cosmemgt.sales.promotion.repository.PromotionRepository;
import com.dtp.cosmemgt.sales.promotion.service.PromotionCalculator;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CartCalculationService {

    ProductVariantRepository productVariantRepository;
    PromotionRepository promotionRepository;
    PromotionCalculator promotionCalculator;

    public CartCalculateResponse calculate(CartCalculateRequest request) {
        BigDecimal subTotal = BigDecimal.ZERO;
        boolean hasProductDiscount = false;

        List<String> variantIds = request.getItems().stream().map(OrderDetailRequest::getProductVariantId).toList();
        Map<String, ProductVariant> variantMap = productVariantRepository.findAllById(variantIds).stream()
                .collect(Collectors.toMap(ProductVariant::getId, v -> v));

        for (OrderDetailRequest item : request.getItems()) {
            ProductVariant variant = variantMap.get(item.getProductVariantId());
            if (variant != null) {
                BigDecimal originalPrice = variant.getUnitPrice();
                BigDecimal priceToCalculate = variant.getDiscountedPrice() != null
                        ? variant.getDiscountedPrice()
                        : originalPrice;

                if (priceToCalculate.compareTo(originalPrice) < 0) {
                    hasProductDiscount = true;
                }

                subTotal = subTotal.add(priceToCalculate.multiply(BigDecimal.valueOf(item.getQty())));
            }
        }

        BigDecimal voucherDiscount = BigDecimal.ZERO;
        BigDecimal finalTotal = subTotal;
        String appliedVoucherCode = null;
        String errorMsg = null;

        // voucher order
        if (request.getVoucherCode() != null && !request.getVoucherCode().trim().isEmpty()) {

            if (hasProductDiscount) {
                errorMsg = "Đơn hàng đã chứa sản phẩm khuyến mãi, không thể áp dụng thêm mã giảm giá đơn hàng.";
            } else {
                Promotion voucher = promotionRepository.findByCode(request.getVoucherCode()).orElse(null);

                if (voucher == null) {
                    errorMsg = "Mã giảm giá không tồn tại.";
                } else if (voucher.getScopeType() != ScopeType.ORDER) {
                    errorMsg = "Mã giảm giá này không áp dụng cho toàn bộ đơn hàng.";
                } else if (!voucher.getIsActive()) {
                    errorMsg = "Mã giảm giá đã bị vô hiệu hóa.";
                } else if (voucher.getStartDate().isAfter(LocalDateTime.now()) || voucher.getEndDate().isBefore(LocalDateTime.now())) {
                    errorMsg = "Mã giảm giá đã hết hạn hoặc chưa tới thời gian sử dụng.";
                } else if (voucher.getUsedCount() >= voucher.getUsageLimit()) {
                    errorMsg = "Mã giảm giá đã hết lượt sử dụng.";
                } else if (voucher.getMinOrderAmount() != null && subTotal.compareTo(voucher.getMinOrderAmount()) < 0) {
                    errorMsg = "Đơn hàng chưa đạt giá trị tối thiểu để dùng mã này.";
                } else {
                    finalTotal = promotionCalculator.calculateDiscountedPrice(subTotal, voucher);
                    voucherDiscount = subTotal.subtract(finalTotal);
                    appliedVoucherCode = voucher.getCode();
                }
            }
        }

        return CartCalculateResponse.builder()
                .subTotal(subTotal)
                .voucherDiscount(voucherDiscount)
                .finalTotal(finalTotal)
                .appliedVoucherCode(appliedVoucherCode)
                .errorMsg(errorMsg)
                .build();
    }
}