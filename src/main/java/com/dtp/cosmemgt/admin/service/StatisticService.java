package com.dtp.cosmemgt.admin.service;

import com.dtp.cosmemgt.admin.dto.response.MonthlyStatisticResponse;
import com.dtp.cosmemgt.admin.dto.response.OverviewStatisticResponse;
import com.dtp.cosmemgt.admin.repository.UserRepository;
import com.dtp.cosmemgt.catalog.dto.response.CategoryShareResponse;
import com.dtp.cosmemgt.catalog.dto.response.InventoryValuationResponse;
import com.dtp.cosmemgt.catalog.dto.response.TopSellingVariantResponse;
import com.dtp.cosmemgt.catalog.repository.CategoryRepository;
import com.dtp.cosmemgt.catalog.repository.ProductRepository;
import com.dtp.cosmemgt.catalog.repository.ProductVariantRepository;
import com.dtp.cosmemgt.sales.order.repository.OrderRepository;
import com.dtp.cosmemgt.warehouse.dto.response.NearExpiryBatchResponse;
import com.dtp.cosmemgt.warehouse.entity.InventoryBatch;
import com.dtp.cosmemgt.warehouse.repository.InventoryBatchRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional(readOnly = true)
public class StatisticService {
    private final InventoryBatchRepository inventoryBatchRepository;
    private final CategoryRepository categoryRepository;
    private final ProductVariantRepository productVariantRepository;
    OrderRepository orderRepository;
    UserRepository userRepository;

    public OverviewStatisticResponse getOverview(LocalDateTime startDate, LocalDateTime endDate) {
        List<Object[]> results = orderRepository.getAllOrderFinancialStatistic(startDate, endDate);

        if (results.isEmpty() || results.get(0) == null) {
            return OverviewStatisticResponse.builder()
                    .totalOrders(0)
                    .successfulOrders(0)
                    .failedOrReturnedOrders(0)
                    .inProgressOrders(0)
                    .totalRevenue(BigDecimal.ZERO)
                    .totalCogs(BigDecimal.ZERO)
                    .totalProfit(BigDecimal.ZERO)
                    .totalCustomers(0)
                    .totalStaff(0)
                    .build();
        }

        Object[] row = results.get(0);

        long totalOrders = parseLongSafely(row[0]);
        long successfulOrders = parseLongSafely(row[1]);
        long failedOrReturnedOrders = parseLongSafely(row[2]);
        long inProgressOrders = parseLongSafely(row[3]);
        BigDecimal totalRevenue = parseBigDecimalSafely(row[4]);
        BigDecimal totalCogs = parseBigDecimalSafely(row[5]);
        BigDecimal totalProfit = totalRevenue.subtract(totalCogs);

        long totalCustomers = userRepository.countByRoleName("USER");
        long totalStaff = userRepository.countByRoleName("ADMIN") + userRepository.countByRoleName("WAREHOUSE");

        return OverviewStatisticResponse.builder()
                .totalOrders(totalOrders)
                .successfulOrders(successfulOrders)
                .failedOrReturnedOrders(failedOrReturnedOrders)
                .inProgressOrders(inProgressOrders)
                .totalRevenue(totalRevenue)
                .totalCogs(totalCogs)
                .totalProfit(totalProfit)
                .totalCustomers(totalCustomers)
                .totalStaff(totalStaff)
                .build();
    }

    public List<MonthlyStatisticResponse> getMonthlyChart(int year) {
        List<MonthlyStatisticResponse> chartData = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            chartData.add(MonthlyStatisticResponse.builder()
                    .month(i)
                    .revenue(BigDecimal.ZERO)
                    .cogs(BigDecimal.ZERO)
                    .profit(BigDecimal.ZERO)
                    .build());
        }

        List<Object[]> results = orderRepository.getMonthlyStatisticsByYear(year);

        for (Object[] row : results) {
            // Ép kiểu an toàn (PostgreSQL EXTRACT thường trả về Double hoặc Integer tùy driver)
            int month = parseIntegerSafely(row[0]);
            BigDecimal revenue = parseBigDecimalSafely(row[1]);
            BigDecimal cogs = parseBigDecimalSafely(row[2]);
            BigDecimal profit = revenue.subtract(cogs);

            // Cập nhật lại phần tử tương ứng (index = month - 1)
            MonthlyStatisticResponse monthData = chartData.get(month - 1);
            monthData.setRevenue(revenue);
            monthData.setCogs(cogs);
            monthData.setProfit(profit);
        }

        return chartData;
    }

    public List<TopSellingVariantResponse> getTopSellingVariants(LocalDateTime startDate, LocalDateTime endDate) {
        List<Object[]> results = productVariantRepository.getTopSellingVariants(startDate, endDate);
        return results.stream().map(row -> TopSellingVariantResponse.builder()
                .sku((String) row[0])
                .variantName((String) row[1])
                .totalSold(parseLongSafely(row[2]))
                .totalRevenue(parseBigDecimalSafely(row[3]))
                .build()).collect(Collectors.toList());
    }

    public List<CategoryShareResponse> getCategoryShare(LocalDateTime startDate, LocalDateTime endDate) {
        List<Object[]> results = categoryRepository.getTheWeightOfCategory(startDate, endDate);
        return results.stream().map(row -> CategoryShareResponse.builder()
                .categoryName((String) row[0])
                .totalSold(parseLongSafely(row[1]))
                .totalRevenue(parseBigDecimalSafely(row[2]))
                .build()).collect(Collectors.toList());
    }

    public InventoryValuationResponse getInventoryValuation() {
        List<Object[]> results = inventoryBatchRepository.getTotalInventoryValue();
        if (results.isEmpty() || results.get(0) == null) {
            return InventoryValuationResponse.builder()
                    .totalPhysicalQty(0)
                    .totalValuation(BigDecimal.ZERO)
                    .build();
        }
        Object[] row = results.get(0);
        return InventoryValuationResponse.builder()
                .totalPhysicalQty(parseLongSafely(row[0]))
                .totalValuation(parseBigDecimalSafely(row[1]))
                .build();
    }

    public List<NearExpiryBatchResponse> getNearExpiryBatches(int alertDays) {
        LocalDate alertDate = LocalDate.now().plusDays(alertDays);
        List<InventoryBatch> batches = inventoryBatchRepository.getNearExpiryBatches(alertDate);

        return batches.stream().map(batch -> NearExpiryBatchResponse.builder()
                .batchId(batch.getId())
                .sku(batch.getProductVariant().getSku())
                .variantName(batch.getProductVariant().getVariantName())
                .remainingQty(batch.getPhysicalQty())
                .expirationDate(batch.getExpirationDate())
                .build()).collect(Collectors.toList());
    }

    // helpers

    private long parseLongSafely(Object value) {
        if (value == null) return 0L;
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return Long.parseLong(value.toString());
    }

    private BigDecimal parseBigDecimalSafely(Object value) {
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        return new BigDecimal(value.toString());
    }

    private int parseIntegerSafely(Object value) {
        if (value == null) return 0;
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return Integer.parseInt(value.toString());
    }
}
