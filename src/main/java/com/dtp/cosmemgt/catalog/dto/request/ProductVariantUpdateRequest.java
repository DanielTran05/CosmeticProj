package com.dtp.cosmemgt.catalog.dto.request;

import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Range;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductVariantUpdateRequest {
    @Size(min = 1, max = 36, message = "PRODUCT_INVALID_LENGTH")
    String product;

    @Range(min = 1, max = 50, message = "UNIT_OF_MEASURE_INVALID_RANGE")
    Integer unitOfMeasure;

    @Size(min = 1, max = 25, message = "SKU_INVALID_LENGTH")
    String sku;

    @Size(min = 1, max = 11, message = "BARCODE_INVALID_LENGTH")
    String barcode;

    @Size(min = 1, max = 225, message = "VARIANT_NAME_INVALID_LENGTH")
    String variantName;

    String img;
}