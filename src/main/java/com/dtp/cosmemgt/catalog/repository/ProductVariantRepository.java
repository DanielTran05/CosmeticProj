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

    //for promotion select
    @Query("select pv from ProductVariant pv where " +
            "LOWER(pv.product.name) LIKE LOWER(CONCAT('%', :kw, '%')) OR " +
            "LOWER(pv.variantName) LIKE LOWER(CONCAT('%', :kw, '%')) " +
            "and pv.deletedAt is null")
    Page<ProductVariant> searchByKeyword(@Param("kw") String kw, Pageable pageable);
    //for promotion select 2
    @Query("select pv from ProductVariant pv where pv.deletedAt is null")
    Page<ProductVariant> findAllSimpleVariant(Pageable pageable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "delete from product_variant where id = :id", nativeQuery = true)
    void hardDelById(@Param("id") String id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "update product_variant SET deleted_at = NULL where id = :id", nativeQuery = true)
    int restoreById(@Param("id") String id);

    @Query(value = "select * from product_variant where id = :id", nativeQuery = true)
    Optional<ProductVariant> findByIdForAdmin(@Param("id") String id);

    //statistic

    @Query(value = "select " +
            "pv.sku AS SKU, " +
            "pv.variant_name AS variantName, " +
            "sum(od.quantity) AS totalSold, " +
            "sum(od.quantity * od.purchased_price) AS totalRevenue " +
            "from order_detail od " +
            "join \"order\" o ON od.order_id = o.id " +
            "join product_variant pv ON od.variant_id = pv.id " +
            "join product p ON p.id = pv.product_id " +
            "where o.order_status = 'COMPLETED' " +
            "and o.created_at BETWEEN :startDate and :endDate " +
            "group by pv.id, pv.sku, pv.variant_name " +
            "order by totalSold DESC " +
            "LIMIT 5", nativeQuery = true)
    List<Object[]> getTopSellingVariants(@Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);
}