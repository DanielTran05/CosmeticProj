package com.dtp.cosmemgt.warehouse.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NearExpiryBatchResponse {
    int batchId;
    String sku;
    String variantName;
    int remainingQty;
    LocalDate expirationDate;
}