package com.dtp.cosmemgt.catalog.dto.response;

import java.math.BigDecimal;

public record BestSellerResponse(
    String productId,
    String productName,
    String slug,
    String avatar,
    BigDecimal basePrice,
    Long totalSold
) {}