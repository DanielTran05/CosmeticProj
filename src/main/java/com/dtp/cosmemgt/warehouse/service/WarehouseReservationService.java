package com.dtp.cosmemgt.warehouse.service;

import com.dtp.cosmemgt.catalog.entity.ProductVariant;
import com.dtp.cosmemgt.catalog.repository.ProductVariantRepository;
import com.dtp.cosmemgt.core.exception.AppException;
import com.dtp.cosmemgt.core.exception.ErrorCode;
import com.dtp.cosmemgt.sales.order.dto.request.OrderCreationRequest;
import com.dtp.cosmemgt.sales.order.dto.request.OrderDetailRequest;
import com.dtp.cosmemgt.sales.order.entity.Order;
import com.dtp.cosmemgt.sales.order.entity.OrderDetail;
import com.dtp.cosmemgt.warehouse.entity.InventoryBatch;
import com.dtp.cosmemgt.warehouse.entity.InventoryTransaction;
import com.dtp.cosmemgt.warehouse.enums.TransactionTypeEnum;
import com.dtp.cosmemgt.warehouse.repository.InventoryBatchRepository;
import com.dtp.cosmemgt.warehouse.repository.InventoryTransactionRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
@Slf4j
public class WarehouseReservationService {
    ProductVariantRepository productVariantRepository;
    InventoryBatchRepository inventoryBatchRepository;
    InventoryTransactionRepository inventoryTransactionRepository;

    public List<InventoryTransaction> reserveInventory(OrderCreationRequest request, Order order) {
        BigDecimal orderTotalAmount = BigDecimal.ZERO;
        BigDecimal orderTotalCogs = BigDecimal.ZERO;

        List<OrderDetail> ods = new ArrayList<>();
        List<InventoryTransaction> transactionsToSave = new ArrayList<>();

        for (OrderDetailRequest odRequest : request.getOrderDetailRequests()) {
            String variantId = odRequest.getProductVariantId();
            int requireQty = odRequest.getQty();

            //FIFO
            List<InventoryBatch> availableBatches = inventoryBatchRepository.findAllAvailableBatchesFIFO(variantId);

            int actualTotalStock = availableBatches.stream().mapToInt(InventoryBatch::getAvailableQty).sum();
            if (actualTotalStock < requireQty) {
                throw new AppException(ErrorCode.OUT_OF_STOCK);
            }

            //tru kho reserve va tong COGS (tong phi san xuat )
            BigDecimal lineTotalCogs = reserveStockAndCalculateCogs(availableBatches, requireQty, transactionsToSave);

            ProductVariant variant = productVariantRepository.findById(variantId)
                    .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_VARIANT_NOT_EXISTED));

            BigDecimal unitCogs = lineTotalCogs.divide(BigDecimal.valueOf(requireQty), 4, RoundingMode.HALF_UP);
            BigDecimal purchasedPrice = variant.getProduct().getBasePrice();

            OrderDetail od = OrderDetail.builder()
                    .productVariant(variant)
                    .order(order)
                    .quantity(requireQty)
                    .purchasedPrice(purchasedPrice)
                    .unitCogs(unitCogs)
                    .build();

            ods.add(od);

            orderTotalAmount = orderTotalAmount.add(purchasedPrice.multiply(BigDecimal.valueOf(requireQty)));
            orderTotalCogs = orderTotalCogs.add(lineTotalCogs);
        }

        order.setTotalAmount(orderTotalAmount);
        order.setTotalCogs(orderTotalCogs);
        order.setOrderDetails(ods);

        return transactionsToSave;
    }

    public void persistReservedTransactions(List<InventoryTransaction> transactionsToSave, Order savedOrder) {
        transactionsToSave.forEach(tx -> tx.setReferenceId(savedOrder.getId()));
        inventoryTransactionRepository.saveAll(transactionsToSave);
    }

    private BigDecimal reserveStockAndCalculateCogs(List<InventoryBatch> batches, int requireQty, List<InventoryTransaction> transactionsToSave) {
        BigDecimal lineTotalCogs = BigDecimal.ZERO;
        int remainingToFulfill = requireQty;

        for (InventoryBatch batch : batches) {
            if (remainingToFulfill == 0) break;

            int qtyToTake = Math.min(batch.getAvailableQty(), remainingToFulfill);
            batch.setAvailableQty(batch.getAvailableQty() - qtyToTake);

            BigDecimal costFromThisBatch = batch.getUnitCost().multiply(BigDecimal.valueOf(qtyToTake));
            lineTotalCogs = lineTotalCogs.add(costFromThisBatch);

            InventoryTransaction transaction = InventoryTransaction.builder()
                    .inventoryBatch(batch)
                    .changeQty(-qtyToTake)
                    .transactionType(TransactionTypeEnum.RESERVE)
                    .build();

            transactionsToSave.add(transaction);
            remainingToFulfill -= qtyToTake;
        }

        return lineTotalCogs;
    }
}
