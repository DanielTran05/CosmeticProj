package com.dtp.cosmemgt.sales.customer.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReviewCreationRequest {
    String productVariantId;
    String customerId;
    int ratingStar;
    String comment;
}
