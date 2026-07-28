package com.dtp.cosmemgt.warehouse.service;

import com.dtp.cosmemgt.admin.entity.User;
import com.dtp.cosmemgt.admin.repository.UserRepository;
import com.dtp.cosmemgt.catalog.repository.ProductVariantRepository;
import com.dtp.cosmemgt.core.exception.AppException;
import com.dtp.cosmemgt.core.exception.ErrorCode;
import com.dtp.cosmemgt.sales.customer.dto.response.OrderResponse;
import com.dtp.cosmemgt.sales.customer.mapper.OrderMapper;
import com.dtp.cosmemgt.sales.entity.Order;
import com.dtp.cosmemgt.sales.enums.OrderStatusEnum;
import com.dtp.cosmemgt.sales.internal.dto.response.WarehouseOrderResponse;
import com.dtp.cosmemgt.sales.internal.mapper.WarehouseOrderMapper;
import com.dtp.cosmemgt.sales.repository.OrderRepository;
import com.dtp.cosmemgt.warehouse.entity.InventoryBatch;
import com.dtp.cosmemgt.warehouse.entity.InventoryTransaction;
import com.dtp.cosmemgt.warehouse.enums.TransactionTypeEnum;
import com.dtp.cosmemgt.warehouse.repository.InventoryBatchRepository;
import com.dtp.cosmemgt.warehouse.repository.InventoryTransactionRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
@Slf4j
public class WarehouseOrderService {
    ProductVariantRepository productVariantRepository;
    UserRepository userRepository;
    OrderRepository orderRepository;
    InventoryBatchRepository inventoryBatchRepository;
    InventoryTransactionRepository inventoryTransactionRepository;

    OrderMapper orderMapper;
    WarehouseOrderMapper warehouseOrderMapper;

    public Page<OrderResponse> getAllOrderToExport(int page, int size){
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Order> orderPage = orderRepository.findAllOrderToExport(pageable);

        return orderPage.map(orderMapper::toOrderResponse);
    }

    public WarehouseOrderResponse getOrderDetailToExport(String orderId){
        Order order = this.getOrder(orderId);

        return warehouseOrderMapper.toWarehouseOrderResponse(order);
    }

    public void orderExportForShipping(String orderId){
        Order o = this.getOrder(orderId);

        if(o.getOrderStatus() != OrderStatusEnum.CONFIRMED)
            throw new AppException(ErrorCode.CAN_NOT_EXPORT_ORDER);

        o.setOrderStatus(OrderStatusEnum.SHIPPING);

        //export inventory transaction
        List<InventoryTransaction> orderInventoryTransactions = inventoryTransactionRepository
                .findAllByReferenceId(o.getId());

        List<InventoryTransaction> newExportTrans = new ArrayList<>();

        for(InventoryTransaction tran : orderInventoryTransactions){
            if(tran.getChangeQty()>=0) continue;

            InventoryBatch b = tran.getInventoryBatch();

            int getExportQty = Math.abs(tran.getChangeQty());
            b.setPhysicalQty(b.getPhysicalQty()-getExportQty);

            InventoryTransaction exportLog = InventoryTransaction.builder()
                    .inventoryBatch(b)
                    .changeQty(-getExportQty)
                    .referenceId(o.getId())
                    .transactionType(TransactionTypeEnum.EXPORT)
                    .build();
            newExportTrans.add(exportLog);
        }

        inventoryTransactionRepository.saveAll(newExportTrans);
    }

    //confirm don huy tu nguoi dung
    public void confirmReturnOrder(String orderId){
        Order o = this.getOrder(orderId);

        OrderStatusEnum orderStatus = o.getOrderStatus();
        if(orderStatus != OrderStatusEnum.SHIPPING
                && orderStatus != OrderStatusEnum.COMPLETED)
            throw new AppException(ErrorCode.CAN_NOT_RETURN_ORDER);

        o.setOrderStatus(OrderStatusEnum.RETURNED);

        this.processInventoryRestoration(o, TransactionTypeEnum.RETURN_ORDER, true);
    }

    //huy don tu phia kho (do don hang hu hong)
    public void cancelOrderFromWarehouse(String orderId){
        Order o = this.getOrder(orderId);

        OrderStatusEnum orderStatus = o.getOrderStatus();
        if(orderStatus != OrderStatusEnum.CONFIRMED)
            throw new AppException(ErrorCode.CAN_NOT_CANCEL_ORDER);

        o.setOrderStatus(OrderStatusEnum.CANCELLED);

        //hoan tien
        //gui email thong bao

        this.processInventoryRestoration(o, TransactionTypeEnum.CANCEL_ORDER, false);
    }

    //xac nhan giao hang thanh cong (webhook goi ve)
    public void confirmDelivered(String orderId){
        Order o = this.getOrder(orderId);

        if(o.getOrderStatus() != OrderStatusEnum.SHIPPING)
            throw new AppException(ErrorCode.CAN_NOT_CONFIRM_DELIVERED);

        o.setOrderStatus(OrderStatusEnum.COMPLETED);
    }




    //utils
    private User getCurrentUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        String userId = authentication.getName();
        if (userId == null || userId.isBlank()) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        return userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    private Order getOrder(String orderId){
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
    }

    private void processInventoryRestoration(Order order,
                                             TransactionTypeEnum transactionType,
                                             boolean isPhysicalReturn) {
        List<InventoryTransaction> trans = inventoryTransactionRepository.findAllByReferenceId(order.getId());
        List<InventoryTransaction> newTransToSave = new ArrayList<>();

        for (InventoryTransaction tran : trans) {
            // Chỉ hoàn lại dựa trên các giao dịch xuất kho (tránh cộng dồn sai nếu có bug logic)
            if (tran.getChangeQty() >= 0) continue;

            InventoryBatch b = tran.getInventoryBatch();

            int refundQty = Math.abs(tran.getChangeQty());

            b.setAvailableQty(b.getAvailableQty() + refundQty);

            // hoan kho vat ly (hang giao bi tra ve)
            if (isPhysicalReturn) {
                b.setPhysicalQty(b.getPhysicalQty() + refundQty);
            }

            InventoryTransaction newTran = InventoryTransaction.builder()
                    .changeQty(refundQty)
                    .inventoryBatch(b)
                    .referenceId(order.getId())
                    .transactionType(transactionType)
                    .build();

            newTransToSave.add(newTran);
        }

        inventoryTransactionRepository.saveAll(newTransToSave);
    }
}
