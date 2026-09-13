package com.dtp.cosmemgt.catalog.repository;

import com.dtp.cosmemgt.catalog.entity.ProductVariant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Transactional
@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, String>, JpaSpecificationExecutor<ProductVariant> {
    boolean existsByVariantName(String variantName);

    boolean existsByUnitOfMeasureId(int uom);

    List<ProductVariant> findByIdIn(List<String> variantIds);

    @Query("SELECT pv FROM ProductVariant pv WHERE " +
            "LOWER(pv.product.name) LIKE LOWER(CONCAT('%', :kw, '%')) OR " +
            "LOWER(pv.variantName) LIKE LOWER(CONCAT('%', :kw, '%'))")
    Page<ProductVariant> searchByKeyword(@Param("kw") String kw, Pageable pageable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "DELETE FROM product_variant WHERE id = :id", nativeQuery = true)
    void hardDelById(@Param("id") String id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE product_variant SET deleted_at = NULL WHERE id = :id", nativeQuery = true)
    int restoreById(@Param("id") String id);

    @Query(value = "SELECT * FROM product_variant WHERE id = :id", nativeQuery = true)
    Optional<ProductVariant> findByIdForAdmin(@Param("id") String id);

    //statistic

    @Query(value = "SELECT " +
            "pv.sku AS SKU, " +
            "pv.variant_name AS variantName, " +
            "SUM(od.quantity) AS totalSold, " +
            "SUM(od.quantity * od.purchased_price) AS totalRevenue " +
            "FROM order_detail od " +
            "JOIN \"order\" o ON od.order_id = o.id " +
            "JOIN product_variant pv ON od.variant_id = pv.id " +
            "JOIN product p ON p.id = pv.product_id " +
            "WHERE o.order_status = 'COMPLETED' " +
            "AND o.created_at BETWEEN :startDate AND :endDate " +
            "GROUP BY pv.id, pv.sku, pv.variant_name " +
            "ORDER BY totalSold DESC " +
            "LIMIT 5", nativeQuery = true)
    List<Object[]> getTopSellingVariants(@Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);
}