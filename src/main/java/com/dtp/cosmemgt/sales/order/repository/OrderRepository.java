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
}