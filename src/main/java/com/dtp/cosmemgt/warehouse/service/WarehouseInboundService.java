package com.dtp.cosmemgt.warehouse.service;

import com.dtp.cosmemgt.warehouse.dto.request.BatchCreationRequest;
import com.dtp.cosmemgt.warehouse.dto.response.BatchResponse;
import com.dtp.cosmemgt.warehouse.entity.InventoryBatch;
import com.dtp.cosmemgt.warehouse.entity.InventoryTransaction;
import com.dtp.cosmemgt.warehouse.enums.TransactionTypeEnum;
import com.dtp.cosmemgt.warehouse.mapper.InventoryBatchMapper;
import com.dtp.cosmemgt.warehouse.repository.InventoryBatchRepository;
import com.dtp.cosmemgt.warehouse.repository.InventoryTransactionRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
@Slf4j
public class WarehouseInboundService {
    InventoryBatchMapper inventoryBatchMapper;

    InventoryBatchRepository inventoryBatchRepository;
    InventoryTransactionRepository inventoryTransactionRepository;

    //tao lo hang moi
    public BatchResponse create(BatchCreationRequest request){
        InventoryBatch b = inventoryBatchMapper.toInventoryBatch(request);

        b.setPhysicalQty(request.getOriginalQty());
        b.setAvailableQty(request.getOriginalQty());

        b = inventoryBatchRepository.save(b);

        InventoryTransaction tran = InventoryTransaction.builder()
                .transactionType(TransactionTypeEnum.IMPORT)
                .changeQty(request.getOriginalQty())
                .inventoryBatch(b)
                .build();

        inventoryTransactionRepository.save(tran);

        return inventoryBatchMapper.toBatchResponse(b);
    }
    //xem danh sach lo hang (theo tung mon hang)

    //canh bao han su dung

    //lich su vao ra cua tung lo hang

    //kiem ke ton kho (stock adjustment)


}
