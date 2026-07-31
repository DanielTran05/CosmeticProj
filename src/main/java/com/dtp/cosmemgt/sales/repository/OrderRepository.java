package com.dtp.cosmemgt.sales.repository;

import com.dtp.cosmemgt.admin.entity.User;
import com.dtp.cosmemgt.catalog.entity.ProductVariant;
import com.dtp.cosmemgt.sales.entity.Order;
import com.dtp.cosmemgt.sales.entity.Review;
import com.dtp.cosmemgt.sales.enums.OrderStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Transactional
@Repository
public interface OrderRepository extends JpaRepository<Order, String>, JpaSpecificationExecutor<Order> {

    @Query("select COUNT(o) > 0 " +
            "from Order o JOIN o.orderDetails od " +
            "WHERE o.customer.id = :userId " +
            "  AND od.productVariant.id = :productVariantId " +
            "  AND o.orderStatus = 'COMPLETED'")
    boolean hasUserPurchasedProduct(@Param("userId") String userId,
                                    @Param("productVariantId") String productVariantId);

    List<Order> findAllByCustomer(User customer);

    @Query("select o " +
            "from Order o " +
            "where o.orderStatus = com.dtp.cosmemgt.sales.enums.OrderStatusEnum.CONFIRMED")
    Page<Order> findAllOrderToExport(Pageable pageable);

    List<Order> findByOrderStatusAndCreatedAtBefore(OrderStatusEnum status, LocalDateTime thresholdTime);
}