package com.dtp.cosmemgt.sales.review.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReviewCreationRequest {
    String productVariantId;
    int ratingStar;
    String comment;
}
