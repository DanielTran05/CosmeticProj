package com.dtp.cosmemgt.catalog.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductUpdateRequest {
    @Size(min = 1, max = 50)
    String name;

    @Digits(integer = 2, fraction = 0, message = "CATE_ID_FORMAT_INVALID")
    Integer cateId;

    @DecimalMin(value = "0.0", inclusive = false, message = "BASE_PRICE_MIN_INVALID")
    @Digits(integer = 9, fraction = 0, message = "BASE_PRICE_FORMAT_INVALID")
    BigDecimal basePrice;

    @Size(min = 1, max = 1, message = "ABC_CLASS_INVALID_LENGTH")
    String abcClass;

    String description;
}