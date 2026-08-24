package com.dtp.cosmemgt.catalog.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Range;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductVariantCreationRequest {
    @NotBlank(message = "PRODUCT_BLANK")
    @Size(min = 1, max = 36, message = "PRODUCT_INVALID_LENGTH")
    String product;

    @NotNull(message = "UNIT_OF_MEASURE_REQUIRED")
    @Range(min = 1, max = 50, message = "UNIT_OF_MEASURE_INVALID_RANGE")
    int unitOfMeasure;

    @NotBlank(message = "SKU_BLANK")
    @Size(min = 1, max = 25, message = "SKU_INVALID_LENGTH")
    String sku;

    @NotBlank(message = "BARCODE_BLANK")
    @Size(min = 1, max = 11, message = "BARCODE_INVALID_LENGTH")
    String barcode;

    @NotBlank(message = "VARIANT_NAME_BLANK")
    @Size(min = 1, max = 225, message = "VARIANT_NAME_INVALID_LENGTH")
    String variantName;

    String img;
}
