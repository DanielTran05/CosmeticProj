package com.dtp.cosmemgt.warehouse.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BatchCreationRequest {
    String productVariantId;
    BigDecimal unitCost;
    Integer originalQty;
    LocalDate expirationDate;
}