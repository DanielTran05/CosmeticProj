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

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000")})
    @Query("select b " +
            "from InventoryBatch b " +
            "where b.productVariant.id in :variantIds " +
            "and b.availableQty > 0 " +
            "and b.expirationDate >= :minValidExpirationDate " +
            "order by b.expirationDate asc, b.createdAt asc")
    List<InventoryBatch> findAllAvailableBatchesFEFOForVariants(
            @Param("variantIds") List<String> variantIds,
            @Param("minValidExpirationDate") LocalDate minValidExpirationDate);

    Page<InventoryBatch> findByExpirationDateBetweenAndAvailableQtyGreaterThan(
            LocalDate fromDate,
            LocalDate toDate,
            int availableQty,
            Pageable pageable);

    @Query("select b from InventoryBatch b " +
            "LEFT join FETCH b.productVariant pv " +
            "LEFT join FETCH pv.product p " +
            "LEFT join FETCH b.supplier " +
            "ORDER BY b.createdAt DESC")
    Page<InventoryBatch> findAllWithVariantAndProduct(Pageable pageable);

    @Query("select b from InventoryBatch b " +
            "LEFT join FETCH b.productVariant pv " +
            "LEFT join FETCH pv.product p " +
            "LEFT join FETCH b.supplier " +
            "where p.id in :productIds " +
            "ORDER BY b.createdAt DESC")
    List<InventoryBatch> findAllByProductIds(@Param("productIds") List<String> productIds);

    //statistic

    @Query("select " +
            "coalesce(sum(b.physicalQty), 0), " +
            "coalesce(sum(b.physicalQty * b.unitCost), 0) " +
            "from InventoryBatch b")
    List<Object[]> getTotalInventoryValue();

    @Query("select b " +
            "from InventoryBatch b " +
            "where b.physicalQty > 0 " +
            "and b.expirationDate BETWEEN CURRENT_DATE and :alertDate " +
            "ORDER BY b.expirationDate ASC")
    List<InventoryBatch> getNearExpiryBatches(@Param("alertDate") LocalDate alertDate);
}