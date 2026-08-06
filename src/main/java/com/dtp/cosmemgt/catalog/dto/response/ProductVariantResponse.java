package com.dtp.cosmemgt.catalog.dto.response;

import com.dtp.cosmemgt.catalog.entity.ProductVariant;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductVariantResponse {
    String id;
    String variantName;
    String img;
    String oum;
}