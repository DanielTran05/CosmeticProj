package com.dtp.cosmemgt.sales.order.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderDetailRequest {
    @NotBlank(message = "PRODUCT_VARIANT_ID_REQUIRED")
//    @Pattern(
//            regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$",
//            message = "INVALID_UUID_FORMAT"
//    )
    String productVariantId;

    @NotNull(message = "QUANTITY_REQUIRED")
    @Min(value = 1, message = "INVALID_QUANTITY")
    Integer qty;
}