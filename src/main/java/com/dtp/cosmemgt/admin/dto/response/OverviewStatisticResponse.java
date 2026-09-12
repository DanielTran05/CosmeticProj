package com.dtp.cosmemgt.admin.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OverviewStatisticResponse {
    BigDecimal totalRevenue;
    BigDecimal totalCogs;
    BigDecimal totalProfit;

    long totalOrders;
    private long successfulOrders; // COMPLETED
    private long failedOrReturnedOrders; // CANCELLED, RETURNED, DELIVERY_FAILED
    private long inProgressOrders; // PENDING, CONFIRMED, PROCESSING, SHIPPING, RETURN_REQUESTED

    long totalCustomers;
    long totalStaff;
}