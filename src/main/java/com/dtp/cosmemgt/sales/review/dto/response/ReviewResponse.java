package com.dtp.cosmemgt.sales.review.dto.response;

import com.dtp.cosmemgt.admin.dto.response.UserResponse;
import com.dtp.cosmemgt.admin.entity.User;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReviewResponse {
    int id;
    String productId;
    String customer;
    int ratingStar;
    String comment;
    LocalDateTime createdAt;
}
