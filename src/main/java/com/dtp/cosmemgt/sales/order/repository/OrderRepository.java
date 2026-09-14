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
    @Query("select o from Order o where o.id = :orderId")
    Optional<Order> findByIdWithLock(@Param("orderId") String orderId);

    @Query("select count(o) > 0 " +
            "from Order o " +
            "join o.orderDetails od " +
            "join od.productVariant pv " +
            "where o.customer.id = :userId " +
            " and pv.product.id = :productId " +
            " and o.orderStatus = 'COMPLETED'")
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
            "where o.id = :orderId and o.orderStatus = 'CONFIRMED'")
    int assignOrderToEmployee(@Param("orderId") String orderId, @Param("employee") User employee);

    @Transactional(readOnly = true)
    @EntityGraph(attributePaths = {"orderDetails", "orderShipping", "employee"})
    Page<Order> findAllByOrderStatusAndEmployeeOrderByCreatedAtAsc(
            OrderStatusEnum orderStatus,
            User employee, Pageable pageable
    );

    //statistic
    @Query(value = "select " +
            "count(id) AS total_orders, " +
            "coalesce(sum(case when order_status = 'COMPLETED' THEN 1 ELSE 0 END), 0) AS successful_orders, " +
            "coalesce(sum(case when order_status in ('CANCELLED', 'RETURNED', 'DELIVERY_FAILED') THEN 1 ELSE 0 END), 0) AS failed_returned_orders, " +
            "coalesce(sum(case when order_status in ('PENDING', 'CONFIRMED', 'PROCESSING', 'SHIPPING', 'RETURN_REQUESTED') THEN 1 ELSE 0 END), 0) AS in_progress_orders, " +
            "coalesce(sum(case when order_status = 'COMPLETED' THEN total_amount ELSE 0 END), 0) AS total_revenue, " +
            "coalesce(sum(case when order_status = 'COMPLETED' THEN total_cogs ELSE 0 END), 0) AS total_cogs " +
            "from \"order\" " +
            "where created_at BETWEEN :startDate and :endDate", nativeQuery = true)
    List<Object[]> getAllOrderFinancialStatistic(@Param("startDate") LocalDateTime startDate,
                                                 @Param("endDate") LocalDateTime endDate);


    @Query(value = "select " +
        "extract(MONTH from created_at) AS month_val, " +
        "coalesce(sum(total_amount), 0) AS total_revenue, " +
        "coalesce(sum(total_cogs), 0) AS total_cogs " +
        "from \"order\" " +
        "where order_status = 'COMPLETED' " +
        "and extract(YEAR from created_at) = :year " +
        "group by extract(MONTH from created_at) " +
        "order by month_val", nativeQuery = true)
    List<Object[]> getMonthlyStatisticsByYear(@Param("year") int year);
}