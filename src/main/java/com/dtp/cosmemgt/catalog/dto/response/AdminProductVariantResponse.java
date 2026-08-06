package com.dtp.cosmemgt.catalog.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminProductVariantResponse {
    String product;
    String unitOfMeasure;
    String sku;
    String barcode;
    String variantName;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    LocalDateTime deletedAt;
    Long deletedBy;
}
