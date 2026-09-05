package com.dtp.cosmemgt.catalog.dto.response;

import java.math.BigDecimal;

public record ProductCardResponse(
        String productId,
        String productName,
        String slug,
        String thumbnail,
        BigDecimal originalMinPrice,
        BigDecimal discountedMinPrice,
        String discountBadge,
        Long totalSold // Có thể null nếu không phải list bán chạy
) {}