package com.dtp.cosmemgt.sales.review.dto.response;

public record ReviewResponseRecord (
        String reviewId,
        String ProductId,
        String productVariantId,
        String customerId,
        int ratingStar,
        String comment
){}
