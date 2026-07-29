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
    String productVariantId;
    BigDecimal unitCost;
    Integer originalQty;
    Integer physicalQty;
    Integer availableQty;
    LocalDate expirationDate;
}
