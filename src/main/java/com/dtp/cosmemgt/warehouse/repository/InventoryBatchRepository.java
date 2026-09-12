package com.dtp.cosmemgt.warehouse.repository;
import com.dtp.cosmemgt.warehouse.entity.InventoryBatch;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Transactional
@Repository
public interface InventoryBatchRepository extends JpaRepository<InventoryBatch, Integer>,
        JpaSpecificationExecutor<InventoryBatch>{

    //lay cac lo hang sap xep theo ngay cu nhat den moi nhat
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000")})
    @Query("select b " +
            "from InventoryBatch b " +
            "where b.productVariant.id in :variantIds and availableQty > 0 " +
            "order by b.createdAt asc")
    List<InventoryBatch> findAllAvailableBatchesFIFOForVariants(@Param("variantIds") List<String> variantIds);

    Page<InventoryBatch> findByExpirationDateLessThanEqualAndAvailableQtyGreaterThan(
            LocalDate thresholdDate,
            int availableQty,
            Pageable pageable);

    @Query("SELECT b FROM InventoryBatch b " +
            "LEFT JOIN FETCH b.productVariant pv " +
            "LEFT JOIN FETCH pv.product p " +
            "LEFT JOIN FETCH b.supplier " +
            "ORDER BY b.createdAt DESC")
    Page<InventoryBatch> findAllWithVariantAndProduct(Pageable pageable);

    @Query("SELECT b FROM InventoryBatch b " +
            "LEFT JOIN FETCH b.productVariant pv " +
            "LEFT JOIN FETCH pv.product p " +
            "LEFT JOIN FETCH b.supplier " +
            "where p.id in :productIds " +
            "ORDER BY b.createdAt DESC")
    List<InventoryBatch> findAllByProductIds(@Param("productIds") List<String> productIds);

    //statistic

    //dinh gia ton kho
    @Query("SELECT " +
            "COALESCE(SUM(b.physicalQty), 0), " +
            "COALESCE(SUM(b.physicalQty * b.unitCost), 0) " +
            "FROM InventoryBatch b")
    List<Object[]> getTotalInventoryValue();

    @Query("SELECT b " +
            "FROM InventoryBatch b " +
            "WHERE b.physicalQty > 0 " +
            "AND b.expirationDate BETWEEN CURRENT_DATE AND :alertDate " +
            "ORDER BY b.expirationDate ASC")
    List<InventoryBatch> getNearExpiryBatches(@Param("alertDate") LocalDate alertDate);
}