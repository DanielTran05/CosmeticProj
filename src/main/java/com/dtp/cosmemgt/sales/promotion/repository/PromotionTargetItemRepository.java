package com.dtp.cosmemgt.sales.promotion.repository;

import com.dtp.cosmemgt.sales.promotion.entity.PromotionTargetItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface PromotionTargetItemRepository extends JpaRepository<PromotionTargetItem, Integer> {
    @Modifying
    @Query(value = "DELETE FROM promotion_target_item WHERE promotion_id = :promotionId", nativeQuery = true)
    void hardDelByPromotionId(@Param("promotionId") String promotionId);

}
