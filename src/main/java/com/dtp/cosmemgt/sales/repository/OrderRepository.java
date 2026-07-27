package com.dtp.cosmemgt.sales.repository;

import com.dtp.cosmemgt.admin.entity.User;
import com.dtp.cosmemgt.catalog.entity.ProductVariant;
import com.dtp.cosmemgt.sales.entity.Order;
import com.dtp.cosmemgt.sales.entity.Review;
import com.dtp.cosmemgt.sales.enums.OrderStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional
@Repository
public interface OrderRepository extends JpaRepository<Order, String>, JpaSpecificationExecutor<Order> {

    @Query("SELECT COUNT(o) > 0 " +
            "FROM Order o JOIN o.orderDetails od " +
            "WHERE o.customer.id = :userId " +
            "  AND od.productVariant.id = :productVariantId " +
            "  AND o.orderStatus = 'COMPLETED'")
    boolean hasUserPurchasedProduct(@Param("userId") String userId,
                                    @Param("productVariantId") String productVariantId);

    List<Order> findAllByCustomer(User customer);

//    boolean existsByName(String name);
//
//    @Modifying
//    @Query(value = "delete from review where id = ?1", nativeQuery = true)
//    void hardDelById(int id);
//
//    @Modifying(clearAutomatically = true)
//    @Query(value = "update review set deleted_at = NULL where id = ?1", nativeQuery = true)
//    int restoreById(int id);
//
//    @Query(value = "select * from review where id = ?1", nativeQuery = true)
//    Optional<Review> getById(int id);
}