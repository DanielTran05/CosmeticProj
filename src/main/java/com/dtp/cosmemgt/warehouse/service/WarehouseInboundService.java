package com.dtp.cosmemgt.warehouse.service;

import com.dtp.cosmemgt.catalog.entity.ProductVariant;
import com.dtp.cosmemgt.catalog.repository.ProductVariantRepository;
import com.dtp.cosmemgt.core.dto.PageResponse;
import com.dtp.cosmemgt.core.exception.AppException;
import com.dtp.cosmemgt.core.exception.ErrorCode;
import com.dtp.cosmemgt.warehouse.dto.request.BatchCreationRequest;
import com.dtp.cosmemgt.warehouse.dto.request.InventoryAdjustmentRequest;
import com.dtp.cosmemgt.warehouse.dto.response.BatchResponse;
import com.dtp.cosmemgt.warehouse.dto.response.InventoryTransactionResponse;
import com.dtp.cosmemgt.warehouse.entity.InventoryBatch;
import com.dtp.cosmemgt.warehouse.entity.InventoryTransaction;
import com.dtp.cosmemgt.warehouse.enums.TransactionTypeEnum;
import com.dtp.cosmemgt.warehouse.mapper.InventoryBatchMapper;
import com.dtp.cosmemgt.warehouse.mapper.InventoryTransactionMapper;
import com.dtp.cosmemgt.warehouse.repository.InventoryBatchRepository;
import com.dtp.cosmemgt.warehouse.repository.InventoryTransactionRepository;
import com.dtp.cosmemgt.warehouse.service.spec.InventoryBatchSpec;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
@Slf4j
public class WarehouseInboundService {
    InventoryBatchMapper inventoryBatchMapper;
    InventoryTransactionMapper inventoryTransactionMapper;

    InventoryBatchRepository inventoryBatchRepository;
    InventoryTransactionRepository inventoryTransactionRepository;
    ProductVariantRepository productVariantRepository;

    //tao lo hang moi
    public BatchResponse create(BatchCreationRequest request){
        ProductVariant pv = productVariantRepository.findById(request.getProductVariantId())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_VARIANT_NOT_EXISTED));

        InventoryBatch b = inventoryBatchMapper.toInventoryBatch(request);
        b.setProductVariant(pv);

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

    // 1. Xem danh sách lô hàng (Áp dụng Spec)
    public PageResponse<BatchResponse> getAllBatches(Map<String, String> queryParams){
        int page = queryParams.containsKey("page") ? Integer.parseInt(queryParams.get("page")) : 0;
        int size = queryParams.containsKey("size") ? Integer.parseInt(queryParams.get("size")) : 10;
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Specification<InventoryBatch> spec = InventoryBatchSpec.filterBatch(queryParams);
        Page<InventoryBatch> batchesPage = inventoryBatchRepository.findAll(spec, pageable);

        return PageResponse.of(batchesPage.map(inventoryBatchMapper::toBatchResponse));
    }

    // 2. Cảnh báo hạn sử dụng (FEFO - First Expired, First Out)
    public PageResponse<BatchResponse> getExpiringBatches(int daysThreshold, int page, int size){
        Pageable pageable = PageRequest.of(page, size, Sort.by("expirationDate").ascending());

        LocalDate thresholdDate = LocalDate.now().plusDays(daysThreshold);

        Page<InventoryBatch> expiringBatches = inventoryBatchRepository
                .findByExpirationDateLessThanEqualAndAvailableQtyGreaterThan(thresholdDate, 0, pageable);

        return PageResponse.of(expiringBatches.map(inventoryBatchMapper::toBatchResponse));
    }

    // 3. Lịch sử vào ra của từng lô hàng
    public PageResponse<InventoryTransactionResponse> getBatchTransactions(String batchId, int page, int size){
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<InventoryTransaction> transactions = inventoryTransactionRepository
                .findAllByInventoryBatchId(batchId, pageable);

        return PageResponse.of(transactions.map(inventoryTransactionMapper::toInventoryTransactionResponse));
    }

    //kiem ke ton kho (stock adjustment)
    public BatchResponse physicalInventoryCount(InventoryAdjustmentRequest request){
        InventoryBatch b = inventoryBatchRepository.findById(request.getBatchId())
                .orElseThrow(() -> new AppException(ErrorCode.INVENTORY_BATCH_NOT_EXISTED));

        int currentPhysicalQty = b.getPhysicalQty();
        int actualPhysicalQty = request.getActualPhysicalQty();

        int diff = actualPhysicalQty-currentPhysicalQty;

        if(diff == 0)
            return inventoryBatchMapper.toBatchResponse(b);

        b.setPhysicalQty(request.getActualPhysicalQty());
        b.setAvailableQty(b.getAvailableQty()+diff);

        if(b.getAvailableQty()<0)
            throw new AppException(ErrorCode.INVALID_ADJUSTMENT_QTY);

        inventoryBatchRepository.save(b);

        InventoryTransaction tran = InventoryTransaction.builder()
                .inventoryBatch( b)
                .changeQty(diff)
                .transactionType(TransactionTypeEnum.ADJUSTMENT)
                .referenceId(String.valueOf(b.getId()))
                .build();

        inventoryTransactionRepository.save(tran);

        return inventoryBatchMapper.toBatchResponse(b);
    }



}
