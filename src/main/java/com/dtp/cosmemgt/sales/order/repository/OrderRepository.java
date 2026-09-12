package com.dtp.cosmemgt.sales.order.repository;

import com.dtp.cosmemgt.admin.entity.User;
import com.dtp.cosmemgt.sales.order.entity.Order;
import com.dtp.cosmemgt.sales.order.enums.OrderStatusEnum;
import com.dtp.cosmemgt.sales.order.enums.PaymentStatusEnum;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Transactional
@Repository
public interface OrderRepository extends JpaRepository<Order, String>, JpaSpecificationExecutor<Order> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Order o WHERE o.id = :orderId")
    Optional<Order> findByIdWithLock(@Param("orderId") String orderId);

    @Query("select COUNT(o) > 0 " +
            "from Order o " +
            "join o.orderDetails od " +
            "join od.productVariant pv " +
            "WHERE o.customer.id = :userId " +
            " AND pv.product.id = :productId " +
            " AND o.orderStatus = 'COMPLETED'")
    boolean hasUserPurchasedAnyVariantOfProduct(@Param("userId") String userId,
                                    @Param("productId") String productId);

    List<Order> findAllByCustomer(User customer);

    @Query("select o " +
            "from Order o " +
            "where o.orderStatus = com.dtp.cosmemgt.sales.order.enums.OrderStatusEnum.CONFIRMED")
    Page<Order> findAllOrderToExport(Pageable pageable);

    List<Order> findByOrderStatusAndCreatedAtBefore(OrderStatusEnum status, LocalDateTime thresholdTime);

    Page<Order> findByOrderStatus(OrderStatusEnum status, Pageable pageable);

    List<Order> findByOrderStatusAndInvoice_PaymentStatus(OrderStatusEnum orderStatus, PaymentStatusEnum paymentStatus);

    @Modifying
    @Query("update Order o set o.orderStatus = 'PROCESSING', o.employee = :employee " +
            "where o.id = :orderId AND o.orderStatus = 'CONFIRMED'")
    int assignOrderToEmployee(@Param("orderId") String orderId, @Param("employee") User employee);

    @Transactional(readOnly = true)
    @EntityGraph(attributePaths = {"orderDetails", "orderShipping", "employee"})
    Page<Order> findAllByOrderStatusAndEmployeeOrderByCreatedAtAsc(
            OrderStatusEnum orderStatus,
            User employee, Pageable pageable
    );

    //statistic
    @Query(value = "SELECT " +
            "COUNT(id) AS total_orders, " +
            "COALESCE(SUM(CASE WHEN order_status = 'COMPLETED' THEN 1 ELSE 0 END), 0) AS successful_orders, " +
            "COALESCE(SUM(CASE WHEN order_status IN ('CANCELLED', 'RETURNED', 'DELIVERY_FAILED') THEN 1 ELSE 0 END), 0) AS failed_returned_orders, " +
            "COALESCE(SUM(CASE WHEN order_status IN ('PENDING', 'CONFIRMED', 'PROCESSING', 'SHIPPING', 'RETURN_REQUESTED') THEN 1 ELSE 0 END), 0) AS in_progress_orders, " +
            "COALESCE(SUM(CASE WHEN order_status = 'COMPLETED' THEN total_amount ELSE 0 END), 0) AS total_revenue, " +
            "COALESCE(SUM(CASE WHEN order_status = 'COMPLETED' THEN total_cogs ELSE 0 END), 0) AS total_cogs " +
            "FROM \"order\" " +
            "WHERE created_at BETWEEN :startDate AND :endDate", nativeQuery = true)
    List<Object[]> getAllOrderFinancialStatistic(@Param("startDate") LocalDateTime startDate,
                                                 @Param("endDate") LocalDateTime endDate);


    @Query(value = "SELECT " +
        "EXTRACT(MONTH FROM created_at) AS month_val, " +
        "COALESCE(SUM(total_amount), 0) AS total_revenue, " +
        "COALESCE(SUM(total_cogs), 0) AS total_cogs " +
        "FROM \"order\" " +
        "WHERE order_status = 'COMPLETED' " +
        "AND EXTRACT(YEAR FROM created_at) = :year " +
        "GROUP BY EXTRACT(MONTH FROM created_at) " +
        "ORDER BY month_val", nativeQuery = true)
    List<Object[]> getMonthlyStatisticsByYear(@Param("year") int year);
}