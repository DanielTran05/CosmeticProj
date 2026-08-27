package com.dtp.cosmemgt.warehouse.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InventoryTransactionResponse {
    Integer changeQty;
    String transactionType;
    LocalDate createdAt;
}
