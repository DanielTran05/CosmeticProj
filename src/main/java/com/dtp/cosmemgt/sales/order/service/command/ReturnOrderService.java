package com.dtp.cosmemgt.sales.order.service.command;

import com.dtp.cosmemgt.admin.entity.User;
import com.dtp.cosmemgt.core.commonService.CurrentUserService;
import com.dtp.cosmemgt.core.commonService.MailService;
import com.dtp.cosmemgt.core.exception.AppException;
import com.dtp.cosmemgt.core.exception.ErrorCode;
import com.dtp.cosmemgt.sales.order.entity.Order;
import com.dtp.cosmemgt.sales.order.enums.OrderStatusEnum;
import com.dtp.cosmemgt.sales.order.enums.PaymentStatusEnum;
import com.dtp.cosmemgt.sales.order.repository.OrderRepository;
import com.dtp.cosmemgt.sales.payment.service.PaymentService;
import com.dtp.cosmemgt.warehouse.entity.InventoryBatch;
import com.dtp.cosmemgt.warehouse.entity.InventoryTransaction;
import com.dtp.cosmemgt.warehouse.enums.TransactionTypeEnum;
import com.dtp.cosmemgt.warehouse.repository.InventoryTransactionRepository;
import com.dtp.cosmemgt.warehouse.service.WarehouseInboundService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
@Slf4j
public class ReturnOrderService {
    InventoryTransactionRepository inventoryTransactionRepository;
    CurrentUserService currentUserService;
    WarehouseInboundService warehouseInboundService;
    OrderRepository orderRepository;

    //customer

    public void returnOrder(String orderId) throws Exception {
        User u = currentUserService.getCurrentUser();
        Order order = getValidOwnedOrder(u, orderId);

        if (order.getOrderStatus() != OrderStatusEnum.COMPLETED) {
            throw new AppException(ErrorCode.CAN_NOT_RETURN_ORDER);
        }

        this.checkValidOrderForReturning(order.getUpdatedAt());

        order.setOrderStatus(OrderStatusEnum.RETURN_REQUESTED);
        log.info("[RETURN] Order [{}] return request submitted. Waiting for warehouse confirmation.", orderId);
    }

    public void cancelReturnRequest(String orderId) throws Exception {
        User u = currentUserService.getCurrentUser();
        Order order = getValidOwnedOrder(u, orderId);

        if (order.getOrderStatus() != OrderStatusEnum.RETURN_REQUESTED) {
            throw new AppException(ErrorCode.INVALID_STATUS_TO_CANCEL_RETURN); // Bạn nhớ thêm ErrorCode này nếu chưa có nhé
        }

        order.setOrderStatus(OrderStatusEnum.COMPLETED);

        if (order.getInvoice() != null && order.getInvoice().getPaymentStatus() != PaymentStatusEnum.UNPAID) {
            order.getInvoice().setPaymentStatus(PaymentStatusEnum.PAID);
        }

        log.info("Khách hàng [{}] đã hủy yêu cầu hoàn trả cho Đơn hàng [{}]. Đơn hàng quay về COMPLETED.", u.getId(), orderId);
    }

    public void mockShipperDeliveryFailed(String orderId){
        Order o = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if(o.getOrderStatus()!=OrderStatusEnum.SHIPPING)
            throw new AppException(ErrorCode.INVALID_STATUS_FOR_DELIVERY_FAILED);

        o.setOrderStatus(OrderStatusEnum.DELIVERY_FAILED);
        log.info("Mock Shipper: Đơn hàng [{}] giao thất bại, đang quay đầu về kho.", orderId);
    }

    //helpers

    private boolean isEligibleForRefund(Order order) {
        return order.getInvoice() != null && order.getInvoice().getPaymentStatus() == PaymentStatusEnum.PAID;
    }

    private Order getValidOwnedOrder(User currentUser, String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (order.getCustomer() == null || !order.getCustomer().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.ORDER_DO_NOT_BELONG);
        }

        return order;
    }

    private void checkValidOrderForReturning(LocalDateTime orderCompletedDate){
        if (orderCompletedDate == null || orderCompletedDate.plusDays(7).isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.RETURN_PERIOD_EXPIRED);
        }
    }
}
