package com.dtp.cosmemgt.warehouse.repository;
import com.dtp.cosmemgt.warehouse.entity.InventoryBatch;
import com.dtp.cosmemgt.warehouse.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional
@Repository
public interface InventoryBatchRepository extends JpaRepository<InventoryBatch, Integer> {

    //so luong mon hang trong tat ca cac lo
    @Query("select coalesce(sum(b.remainQty), 0) " +
            "from InventoryBatch b " +
            "where b.productVariant.id = :variantId " +
            "and b.remainQty > 0")
    int getTotalStockByVariant(@Param("variantId") String variantId);

    //lay cac lo hang sap xep theo ngay cu nhat den moi nhat
    @Query("select * " +
            "from InventoryBatch b " +
            "where b.productVariant.id = :variantId and remainQty > 0 " +
            "order by b.createdAt asc")
    List<InventoryBatch> findAllAvailableBatchesFIFO(@Param("variantId") String variantId);
//    boolean existsByName(String name);
//
//    @Modifying
//    @Query(value = "delete from supplier where id = ?1", nativeQuery = true)
//    void hardDelById(int id);
//
//    @Modifying(clearAutomatically = true)
//    @Query(value = "update supplier set deleted_at = NULL where id = ?1", nativeQuery = true)
//    int restoreById(int id);
//
//    @Query(value = "select * from supplier where id = ?1", nativeQuery = true)
//    Optional<Supplier> getById(int id);
}