package com.dtp.cosmemgt.sales.promotion.repository;

import com.dtp.cosmemgt.sales.promotion.entity.Promotion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public interface PromotionRepository extends JpaRepository<Promotion, String> {
    boolean existsByCode(String code);

    Page<Promotion> findAll(Specification<Promotion> specification, Pageable pageable);

    @Query("select p from Promotion p where p.scopeType = 'ORDER' and p.isActive = true " +
            "and p.startDate <= :now and p.endDate >= :now and p.usedCount < p.usageLimit")
    List<Promotion> findAvailableOrderVouchers(@Param("now") LocalDateTime now);

    @Modifying(clearAutomatically = true)
    @Query(value = "update promotion set deleted_at = null, deleted_by = null " +
            "where id = :id and deleted_at is not null", nativeQuery = true)
    int restorePromotion(@Param("id") String promotionId);

    @Query(value = "select * from promotion where id = :id", nativeQuery = true)
    Optional<Promotion> getPromotionById(@Param("id") String promotionId);

    @Modifying
    @Query(value = "delete from promotion where id = :id", nativeQuery = true)
    void hardDelById(@Param("id") String promotionId);


    //apply promotion
    @Query("SELECT p FROM Promotion p JOIN p.targetItems t " +
            "WHERE p.isAutoApplied = true " +
            "AND p.scopeType IN ('PRODUCT', 'VARIANT') " +
            "AND p.startDate <= :now AND p.endDate >= :now " +
            "AND p.usedCount < p.usageLimit " +
            "AND (t.targetId = :productId OR t.targetId IN :variantIds) " +
            "and p.deletedAt is null and p.isActive = true")
    List<Promotion> findActiveProductPromotions(@Param("productId") String productId,
                                                @Param("variantIds") List<String> variantIds,
                                                @Param("now") LocalDateTime now);

    @Modifying
    @Query("update Promotion p SET p.usedCount = p.usedCount + 1 " +
            "where p.id = :promotionId and p.usedCount < p.usageLimit")
    int incrementUsedCount(@Param("promotionId") String promotionId);

    Optional<Promotion> findByCode(String code);
}