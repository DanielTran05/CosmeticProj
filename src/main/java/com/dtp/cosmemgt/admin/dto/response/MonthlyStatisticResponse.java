package com.dtp.cosmemgt.admin.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MonthlyStatisticResponse {
    int month;
    BigDecimal revenue;
    BigDecimal cogs;
    BigDecimal profit;
}