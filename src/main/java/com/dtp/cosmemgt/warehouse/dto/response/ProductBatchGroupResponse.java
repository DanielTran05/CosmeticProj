package com.dtp.cosmemgt.warehouse.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductBatchGroupResponse {
    String productId;
    String productName;
    List<BatchResponse> batches;
}