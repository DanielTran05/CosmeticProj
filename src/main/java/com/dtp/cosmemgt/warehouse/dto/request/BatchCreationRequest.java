package com.dtp.cosmemgt.warehouse.dto.request;

import jakarta.validation.constraints.NotNull;
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
    @NotNull(message = "Product Variant ID is required")
    String productVariantId;

    @NotNull(message = "Supplier ID is required")
    Integer supplierId;

    BigDecimal unitCost;
    Integer originalQty;
    LocalDate expirationDate;
}