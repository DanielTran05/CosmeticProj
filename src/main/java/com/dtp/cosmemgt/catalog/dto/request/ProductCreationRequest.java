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
public class ProductCreationRequest {
    @NotBlank(message = "PRODUCT_NAME_BLANK")
    @Size(min = 1, max = 50)
    String name;

    @NotNull
    @Digits(integer = 2, fraction = 0, message = "CATE_ID_FORMAT_INVALID")
    Integer cateId;

    @NotBlank(message = "ABC_CLASS_BLANK")
    @Size(min = 1, max = 1, message = "ABC_CLASS_INVALID_LENGTH")
    String abcClass;

    String description;
}