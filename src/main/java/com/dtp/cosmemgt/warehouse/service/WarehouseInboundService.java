package com.dtp.cosmemgt.warehouse.service;

import com.dtp.cosmemgt.catalog.entity.Product;
import com.dtp.cosmemgt.catalog.entity.ProductVariant;
import com.dtp.cosmemgt.catalog.repository.ProductRepository;
import com.dtp.cosmemgt.catalog.repository.ProductVariantRepository;
import com.dtp.cosmemgt.core.dto.PageResponse;
import com.dtp.cosmemgt.core.exception.AppException;
import com.dtp.cosmemgt.core.exception.ErrorCode;
import com.dtp.cosmemgt.sales.order.entity.Order;
import com.dtp.cosmemgt.warehouse.dto.request.BatchCreationRequest;
import com.dtp.cosmemgt.warehouse.dto.request.InventoryAdjustmentRequest;
import com.dtp.cosmemgt.warehouse.dto.response.BatchResponse;
import com.dtp.cosmemgt.warehouse.dto.response.InventoryTransactionResponse;
import com.dtp.cosmemgt.warehouse.dto.response.ProductBatchGroupResponse;
import com.dtp.cosmemgt.warehouse.entity.InventoryBatch;
import com.dtp.cosmemgt.warehouse.entity.InventoryTransaction;
import com.dtp.cosmemgt.warehouse.entity.Supplier;
import com.dtp.cosmemgt.warehouse.enums.TransactionTypeEnum;
import com.dtp.cosmemgt.warehouse.mapper.InventoryBatchMapper;
import com.dtp.cosmemgt.warehouse.mapper.InventoryTransactionMapper;
import com.dtp.cosmemgt.warehouse.repository.InventoryBatchRepository;
import com.dtp.cosmemgt.warehouse.repository.InventoryTransactionRepository;
import com.dtp.cosmemgt.warehouse.repository.SupplierRepository;
import com.dtp.cosmemgt.warehouse.service.spec.InventoryBatchSpec;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

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
    SupplierRepository supplierRepository;
    ProductRepository productRepository;

    public BatchResponse create(BatchCreationRequest request){
        ProductVariant pv = productVariantRepository.findById(request.getProductVariantId())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_VARIANT_NOT_EXISTED));

        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new AppException(ErrorCode.SUPPLIER_NOT_EXISTED));

        // 3. Map request to Entity
        InventoryBatch b = inventoryBatchMapper.toInventoryBatch(request);
        b.setProductVariant(pv);
        b.setSupplier(supplier);

        b.setPhysicalQty(request.getOriginalQty());
        b.setAvailableQty(request.getOriginalQty());

        b = inventoryBatchRepository.save(b);

        // 4. Lưu lịch sử giao dịch
        InventoryTransaction tran = InventoryTransaction.builder()
                .transactionType(TransactionTypeEnum.IMPORT)
                .changeQty(request.getOriginalQty())
                .inventoryBatch(b)
                .build();

        inventoryTransactionRepository.save(tran);

        return inventoryBatchMapper.toBatchResponse(b);
    }

    public PageResponse<BatchResponse> getAllBatches(Map<String, String> queryParams){
        int page = queryParams.containsKey("page") ? Integer.parseInt(queryParams.get("page")) : 0;
        int size = queryParams.containsKey("size") ? Integer.parseInt(queryParams.get("size")) : 10;
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Specification<InventoryBatch> spec = InventoryBatchSpec.filterBatch(queryParams);
        Page<InventoryBatch> batchesPage = inventoryBatchRepository.findAll(spec, pageable);

        return PageResponse.of(batchesPage.map(inventoryBatchMapper::toBatchResponse));
    }

    public PageResponse<ProductBatchGroupResponse> getBatchesGroupedByProduct(String kw, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage;

        if(kw!=null && !kw.trim().isEmpty())
            productPage = productRepository.findByNameContainingIgnoreCase(kw.trim(), pageable);
        else
            productPage = productRepository.findAll(pageable);

        if (productPage.isEmpty()) {
            return PageResponse.of(new PageImpl<>(new ArrayList<>(), pageable, productPage.getTotalElements()));
        }

        List<String> productIds = productPage.getContent().stream()
                .map(Product::getId)
                .toList();

        List<InventoryBatch> allBatches = inventoryBatchRepository.findAllByProductIds(productIds);

        Map<String, ProductBatchGroupResponse> groupMap = new LinkedHashMap<>();

        for (Product p : productPage.getContent()) {
            groupMap.put(String.valueOf(p.getId()), ProductBatchGroupResponse.builder()
                    .productId(String.valueOf(p.getId()))
                    .productName(p.getName())
                    .batches(new ArrayList<>())
                    .build());
        }

        for (InventoryBatch batch : allBatches) {
            if (batch.getProductVariant() != null && batch.getProductVariant().getProduct() != null) {
                String pId = String.valueOf(batch.getProductVariant().getProduct().getId());

                BatchResponse batchRes = inventoryBatchMapper.toBatchResponse(batch);
                batchRes.setVariantName(batch.getProductVariant().getVariantName());

                if (groupMap.containsKey(pId)) {
                    groupMap.get(pId).getBatches().add(batchRes);
                }
            }
        }

        List<ProductBatchGroupResponse> groupedList = new ArrayList<>(groupMap.values());
        Page<ProductBatchGroupResponse> groupedPage = new PageImpl<>(groupedList, pageable, productPage.getTotalElements());

        return PageResponse.of(groupedPage);
    }

    public PageResponse<BatchResponse> getExpiringBatches(int daysThreshold, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("expirationDate").ascending());

        LocalDate today = LocalDate.now();
        LocalDate thresholdDate = today.plusDays(daysThreshold);

        Page<InventoryBatch> expiringBatches = inventoryBatchRepository
                .findByExpirationDateBetweenAndAvailableQtyGreaterThan(today, thresholdDate, 0, pageable);

        return PageResponse.of(expiringBatches.map(inventoryBatchMapper::toBatchResponse));
    }

    public PageResponse<InventoryTransactionResponse> getBatchTransactions(int batchId, int page, int size){
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<InventoryTransaction> transactions = inventoryTransactionRepository
                .findAllByInventoryBatchId(batchId, pageable);

        return PageResponse.of(transactions.map(inventoryTransactionMapper::toInventoryTransactionResponse));
    }

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

    public void processInventoryRestoration(Order order, TransactionTypeEnum transactionType, boolean isPhysicalReturn) {
        List<InventoryTransaction> trans = inventoryTransactionRepository.findAllByReferenceId(order.getId());

        List<InventoryTransaction> newTransToSave = trans.stream()
                .filter(tran -> {
                    if (isPhysicalReturn) {
                        return tran.getTransactionType() == TransactionTypeEnum.EXPORT;
                    } else {
                        return tran.getTransactionType() == TransactionTypeEnum.RESERVE;
                    }
                })
                .map(tran -> {
                    InventoryBatch b = tran.getInventoryBatch();
                    int refundQty = Math.abs(tran.getChangeQty());

                    b.setAvailableQty(b.getAvailableQty() + refundQty);

                    if (isPhysicalReturn) {
                        b.setPhysicalQty(b.getPhysicalQty() + refundQty);
                    }

                    return InventoryTransaction.builder()
                            .changeQty(refundQty)
                            .inventoryBatch(b)
                            .referenceId(order.getId())
                            .transactionType(transactionType)
                            .build();
                })
                .toList();

        inventoryTransactionRepository.saveAll(newTransToSave);
    }
}
