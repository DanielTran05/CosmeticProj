package com.dtp.cosmemgt.catalog.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CategoryShareResponse {
    String categoryName;
    long totalSold;
    BigDecimal totalRevenue;
}