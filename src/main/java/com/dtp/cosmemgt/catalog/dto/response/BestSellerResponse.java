package com.dtp.cosmemgt.catalog.dto.response;

public record BestSellerResponse(
    String variantId,
    String productName,
    String variantName,
    Long totalSold
) {}