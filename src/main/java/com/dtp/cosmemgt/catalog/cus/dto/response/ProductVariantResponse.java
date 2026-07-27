package com.dtp.cosmemgt.catalog.cus.dto.response;

import com.dtp.cosmemgt.catalog.entity.ProductVariant;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductVariantResponse {
    String id;
    String variantName;
    String img;
}