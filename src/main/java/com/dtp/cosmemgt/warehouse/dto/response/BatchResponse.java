package com.dtp.cosmemgt.warehouse.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BatchResponse {
    Integer id;
    String productVariantId;
    String variantName;
    String supplierName;
    BigDecimal unitCost;
    Integer originalQty;
    Integer physicalQty;
    Integer availableQty;
    LocalDate expirationDate;
    LocalDate createdAt;
}