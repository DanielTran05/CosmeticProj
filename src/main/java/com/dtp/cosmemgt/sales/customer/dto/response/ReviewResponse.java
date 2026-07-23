package com.dtp.cosmemgt.sales.customer.dto.response;

import com.dtp.cosmemgt.admin.entity.User;
import com.dtp.cosmemgt.sales.entity.Customer;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

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
