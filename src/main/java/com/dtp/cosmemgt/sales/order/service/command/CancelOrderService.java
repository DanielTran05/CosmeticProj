package com.dtp.cosmemgt.sales.order.service.command;

import com.dtp.cosmemgt.admin.entity.User;
import com.dtp.cosmemgt.catalog.entity.ProductVariant;
import com.dtp.cosmemgt.core.commonService.CurrentUserService;
import com.dtp.cosmemgt.core.commonService.MailService;
import com.dtp.cosmemgt.core.exception.AppException;
import com.dtp.cosmemgt.core.exception.ErrorCode;
import com.dtp.cosmemgt.sales.order.entity.Order;
import com.dtp.cosmemgt.sales.order.entity.OrderDetail;
import com.dtp.cosmemgt.sales.order.enums.OrderStatusEnum;
import com.dtp.cosmemgt.sales.order.enums.PaymentStatusEnum;
import com.dtp.cosmemgt.sales.order.repository.OrderRepository;
import com.dtp.cosmemgt.sales.payment.dto.request.PaymentFailedEvent;
import com.dtp.cosmemgt.sales.promotion.service.OrderPromotionUsageService;
import com.dtp.cosmemgt.warehouse.enums.TransactionTypeEnum;
import com.dtp.cosmemgt.warehouse.service.WarehouseInboundService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional(rollbackFor = Exception.class)
@Slf4j
public class CancelOrderService {
    CurrentUserService currentUserService;
    WarehouseInboundService warehouseInboundService;
    MailService mailService;
    OrderPromotionUsageService orderPromotionUsageService;
    OrderRepository orderRepository;

    public void cancelOrder(String orderId) throws Exception {
        User u = currentUserService.getCurrentUser();
        Order order = getValidOwnedOrderWithLock(u, orderId);

        if (order.getOrderStatus() != OrderStatusEnum.PENDING
                && order.getOrderStatus() != OrderStatusEnum.CONFIRMED) {
            throw new AppException(ErrorCode.CAN_NOT_CANCEL_ORDER);
        }

        if (isEligibleForRefund(order)) {
            order.getInvoice().setPaymentStatus(PaymentStatusEnum.PENDING_REFUND);
            mailService.sendOrderConfirmRefundEmail(u, order);
        } else {
            if (order.getInvoice() != null) {
                order.getInvoice().setPaymentStatus(PaymentStatusEnum.CANCELLED);
            }
        }

        List<ProductVariant> productVariants = order.getOrderDetails().stream()
                .map(OrderDetail::getProductVariant)
                .toList();
        orderPromotionUsageService.restoreProductPromotions(productVariants);

        if (order.getVoucherCode() != null) {
            orderPromotionUsageService.restoreOrderVoucher(order.getVoucherCode());
        }

        warehouseInboundService.processInventoryRestoration(order, TransactionTypeEnum.CANCEL_ORDER, false);

        order.setOrderStatus(OrderStatusEnum.CANCELLED);

        log.info("Order [{}] cancelled successfully by user [{}]", orderId, u.getId());
    }

    public void cancelOrderDueToPaymentFailure(String orderId, boolean isFromPaymentFailedEvent) {
        Order o = orderRepository.findByIdWithLock(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (o.getOrderStatus() != OrderStatusEnum.PENDING) {
            return;
        }

        o.setOrderStatus(OrderStatusEnum.CANCELLED);

        if (isFromPaymentFailedEvent) {
            o.getInvoice().setPaymentStatus(PaymentStatusEnum.FAILED);
        } else {
            o.getInvoice().setPaymentStatus(PaymentStatusEnum.CANCELLED);
        }

        List<ProductVariant> productVariants = o.getOrderDetails().stream()
                .map(OrderDetail::getProductVariant)
                .toList();
        orderPromotionUsageService.restoreProductPromotions(productVariants);

        if (o.getVoucherCode() != null) {
            orderPromotionUsageService.restoreOrderVoucher(o.getVoucherCode());
        }

        warehouseInboundService.processInventoryRestoration(o, TransactionTypeEnum.CANCEL_ORDER, false);
    }

    private boolean isEligibleForRefund(Order order) {
        return order.getInvoice() != null && order.getInvoice().getPaymentStatus() == PaymentStatusEnum.PAID;
    }

    private Order getValidOwnedOrderWithLock(User currentUser, String orderId) {
        Order order = orderRepository.findByIdWithLock(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (order.getCustomer() == null || !order.getCustomer().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.ORDER_DO_NOT_BELONG);
        }

        return order;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handlePaymentFailedEvent(PaymentFailedEvent event) {
        String orderId = event.getOrderId();
        log.info("Received PaymentFailedEvent for Order ID: {}", orderId);
        cancelOrderDueToPaymentFailure(orderId, true);
    }
}