package com.dtp.cosmemgt.warehouse.repository;
import com.dtp.cosmemgt.warehouse.entity.InventoryBatch;
import com.dtp.cosmemgt.warehouse.entity.InventoryTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Integer> {
    List<InventoryTransaction> findAllByReferenceId(String referenceId);

}