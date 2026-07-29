package com.dtp.cosmemgt.warehouse.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InventoryAdjustmentRequest {
    Integer batchId;
    Integer actualPhysicalQty;
    String reason;
}