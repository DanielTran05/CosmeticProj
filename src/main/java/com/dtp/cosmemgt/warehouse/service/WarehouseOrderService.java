package com.dtp.cosmemgt.warehouse.service;

import com.dtp.cosmemgt.admin.entity.User;
import com.dtp.cosmemgt.admin.repository.UserRepository;
import com.dtp.cosmemgt.core.commonService.MailService;
import com.dtp.cosmemgt.core.dto.PageResponse;
import com.dtp.cosmemgt.core.exception.AppException;
import com.dtp.cosmemgt.core.exception.ErrorCode;
import com.dtp.cosmemgt.sales.order.dto.response.OrderResponse;
import com.dtp.cosmemgt.sales.order.mapper.OrderMapper;
import com.dtp.cosmemgt.sales.payment.service.PaymentService;
import com.dtp.cosmemgt.sales.order.entity.Order;
import com.dtp.cosmemgt.sales.order.enums.OrderStatusEnum;
import com.dtp.cosmemgt.sales.order.enums.PaymentStatusEnum;
import com.dtp.cosmemgt.sales.review.dto.response.WarehouseOrderResponse;
import com.dtp.cosmemgt.warehouse.mapper.WarehouseOrderMapper;
import com.dtp.cosmemgt.sales.order.repository.OrderRepository;
import com.dtp.cosmemgt.warehouse.entity.InventoryBatch;
import com.dtp.cosmemgt.warehouse.entity.InventoryTransaction;
import com.dtp.cosmemgt.warehouse.enums.TransactionTypeEnum;
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
    UserRepository userRepository;
    OrderRepository orderRepository;
    InventoryTransactionRepository inventoryTransactionRepository;
    PaymentService paymentService;

    OrderMapper orderMapper;
    WarehouseOrderMapper warehouseOrderMapper;

    //COMMAND

    public void orderExportForShipping(String orderId){
        Order o = this.getOrder(orderId);

        if(o.getOrderStatus() != OrderStatusEnum.CONFIRMED &&
            o.getInvoice().getPaymentStatus() != PaymentStatusEnum.PAID)
            throw new AppException(ErrorCode.CAN_NOT_EXPORT_ORDER);

        o.setOrderStatus(OrderStatusEnum.SHIPPING);

        //create new export inventory transaction
        List<InventoryTransaction> orderInventoryTransactions = inventoryTransactionRepository
                .findAllByReferenceId(o.getId());

        this.createOrderExportTransaction(orderInventoryTransactions, o.getId());
    }

    public void warehousConfirmReturnOrder(String orderId) throws Exception {
        Order o = this.getOrder(orderId);

        if(o.getOrderStatus() != OrderStatusEnum.RETURN_REQUESTED) {
            throw new AppException(ErrorCode.CAN_NOT_RETURN_ORDER);
        }

        if (o.getInvoice() != null &&
                o.getInvoice().getPaymentStatus() == PaymentStatusEnum.PAID) {
            o.getInvoice().setPaymentStatus(PaymentStatusEnum.PENDING_REFUND);
        }

        this.processInventoryRestoration(o, TransactionTypeEnum.RETURN_ORDER, true);

        o.setOrderStatus(OrderStatusEnum.RETURNED);
        log.info("Warehouse confirmed return and refunded order [{}]", orderId);
    }

    public void warehouseConfirmFailedOrder(String orderId){
        Order o = getOrder(orderId);

        if(o.getOrderStatus()!=OrderStatusEnum.DELIVERY_FAILED)
            throw new AppException(ErrorCode.INVALID_STATUS_FOR_DELIVERY_FAILED);

        if (o.getInvoice() != null &&
                o.getInvoice().getPaymentStatus() == PaymentStatusEnum.PAID) {
            o.getInvoice().setPaymentStatus(PaymentStatusEnum.PENDING_REFUND);
        }

        processInventoryRestoration(o, TransactionTypeEnum.RETURN_ORDER, true);

        o.setOrderStatus(OrderStatusEnum.RETURNED);

        log.info("Warehouse xác nhận đã nhập lại kho đơn hàng Boom [{}].", orderId);
    }

    public void cancelOrderFromWarehouse(String orderId) throws Exception {
        Order o = this.getOrder(orderId);

        if(o.getOrderStatus() != OrderStatusEnum.CONFIRMED) {
            throw new AppException(ErrorCode.CAN_NOT_CANCEL_ORDER);
        }

        if (o.getInvoice() != null && o.getInvoice().getPaymentStatus() == PaymentStatusEnum.PAID) {
            o.getInvoice().setPaymentStatus(PaymentStatusEnum.PENDING_REFUND);
        }

        this.processInventoryRestoration(o, TransactionTypeEnum.CANCEL_ORDER, false);

        o.setOrderStatus(OrderStatusEnum.CANCELLED);
        log.info("Warehouse cancelled and refunded order [{}]", orderId);
    }

    public void markOrderCompleted(String orderId){             //danh cho shipping provider
        Order o = this.getOrder(orderId);

        if(o.getOrderStatus() != OrderStatusEnum.SHIPPING)
            throw new AppException(ErrorCode.CAN_NOT_MARK_ORDER_COMPLETED);

        o.setOrderStatus(OrderStatusEnum.COMPLETED);
    }


    //QUERY

    public PageResponse<OrderResponse> getAllOrder(OrderStatusEnum status, int page, int size){
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Order> orderPage;

        if (status != null) {
            orderPage = orderRepository.findByOrderStatus(status, pageable);
        } else {
            orderPage = orderRepository.findAll(pageable);
        }

        return PageResponse.of(orderPage.map(orderMapper::toOrderResponse));
    }

    public WarehouseOrderResponse getOrderDetailToExport(String orderId){
        Order order = this.getOrder(orderId);

        return warehouseOrderMapper.toWarehouseOrderResponse(order);
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
            if (tran.getChangeQty() >= 0) continue;

            InventoryBatch b = tran.getInventoryBatch();

            int refundQty = Math.abs(tran.getChangeQty());

            b.setAvailableQty(b.getAvailableQty() + refundQty);

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

    private void createOrderExportTransaction(List<InventoryTransaction> transactions, String orderId){
        List<InventoryTransaction> newExportTrans = new ArrayList<>();

        for(InventoryTransaction tran : transactions){
            if(tran.getChangeQty()>=0) continue;

            InventoryBatch b = tran.getInventoryBatch();

            int getExportQty = Math.abs(tran.getChangeQty());
            b.setPhysicalQty(b.getPhysicalQty()-getExportQty);

            InventoryTransaction exportLog = InventoryTransaction.builder()
                    .inventoryBatch(b)
                    .changeQty(-getExportQty)
                    .referenceId(orderId)
                    .transactionType(TransactionTypeEnum.EXPORT)
                    .build();
            newExportTrans.add(exportLog);
        }

        inventoryTransactionRepository.saveAll(newExportTrans);
    }
}
