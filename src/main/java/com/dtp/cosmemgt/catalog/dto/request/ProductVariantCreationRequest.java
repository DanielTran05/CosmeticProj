package com.dtp.cosmemgt.catalog.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductVariantCreationRequest {
    String product;
    Integer unitOfMeasure;
    String sku;
    String barcode;
    String variantName;
}
