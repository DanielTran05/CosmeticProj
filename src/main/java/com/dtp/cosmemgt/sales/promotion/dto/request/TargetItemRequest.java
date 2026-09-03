package com.dtp.cosmemgt.sales.promotion.dto.request;

import com.dtp.cosmemgt.sales.promotion.enums.TargetType;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TargetItemRequest {
    private String targetId;
    private TargetType targetType;
}