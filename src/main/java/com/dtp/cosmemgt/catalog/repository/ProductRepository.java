package com.dtp.cosmemgt.catalog.repository;

import com.dtp.cosmemgt.catalog.dto.response.BestSellerProductProjection;
import com.dtp.cosmemgt.catalog.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional
@Repository
public interface ProductRepository extends JpaRepository<Product, String>,
                                        JpaSpecificationExecutor<Product> {
    boolean existsByName(String name);

    @Query(value = """
        SELECT DISTINCT p.* FROM product p
        JOIN product_variant pv ON p.id = pv.product_id
        JOIN category c ON c.id = p.category_id
        JOIN promotion_target_item pti ON (pti.target_id = p.id AND pti.target_type = 'PRODUCT')
                                       OR (pti.target_id = pv.id AND pti.target_type = 'VARIANT' AND pv.deleted_at IS NULL)
        JOIN promotion promo ON pti.promotion_id = promo.id
        WHERE p.deleted_at IS NULL
          AND c.deleted_at IS NULL
          AND pv.deleted_at IS NULL
          AND promo.is_active = true
          AND CURRENT_TIMESTAMP BETWEEN promo.start_date AND promo.end_date
        LIMIT 12
    """, nativeQuery = true)
    List<Product> findProductCurrentlyOnSale();

    Optional<Product> findBySlug(String slug);

    boolean existsBySlug(String slug);

    @Query(value = "select p " +
            "from Product p left join fetch p.productVariants " +
            "where p.id = :productId and p.deletedAt is null")
    Optional<Product> findByIdWithVariants(String productId);

    @Modifying
    @Query(value = "delete from product where id = ?1", nativeQuery = true)
    void hardDelById(String id);

    @Modifying(clearAutomatically = true)
    @Query(value = "update product set deleted_at = NULL where id = ?1", nativeQuery = true)
    int restoreById(String id);

    @Query(value = "select * from product where id = ?1", nativeQuery = true)
    Optional<Product> getProductById(String id);

    @Query("select p AS product, sum(od.quantity) AS totalSold " +
            "from OrderDetail od " +
            "join od.order o " +
            "join od.productVariant pv " +
            "join pv.product p " +
            "join p.category c " +
            "where o.orderStatus = com.dtp.cosmemgt.sales.order.enums.OrderStatusEnum.COMPLETED " +
            "and p.deletedAt IS NULL " +
            "and c.deletedAt is null " +
            "group by p " +
            "order by sum(od.quantity) DESC " +
            "limit 12")
    List<BestSellerProductProjection> findTop12BestSellingProducts();

    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);
}