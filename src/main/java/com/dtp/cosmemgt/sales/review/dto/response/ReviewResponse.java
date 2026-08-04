package com.dtp.cosmemgt.sales.review.dto.response;

import com.dtp.cosmemgt.admin.entity.User;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReviewResponse {
    String productVariantId;
    User customer;
    int ratingStar;
    String comment;
}
