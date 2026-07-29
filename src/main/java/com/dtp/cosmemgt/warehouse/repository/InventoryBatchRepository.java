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
import java.util.List;

@Transactional
@Repository
public interface InventoryBatchRepository extends JpaRepository<InventoryBatch, Integer>
        , JpaSpecificationExecutor<InventoryBatch>{
    //lay cac lo hang sap xep theo ngay cu nhat den moi nhat
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "javax.persistence.lock.timeout", value = "3000")})
    @Query("select b " +
            "from InventoryBatch b " +
            "where b.productVariant.id = :variantId and availableQty > 0 " +
            "order by b.createdAt asc")
    List<InventoryBatch> findAllAvailableBatchesFIFO(@Param("variantId") String variantId);

    Page<InventoryBatch> findByExpirationDateLessThanEqualAndAvailableQtyGreaterThan(
            LocalDate thresholdDate,
            int availableQty,
            Pageable pageable);
}