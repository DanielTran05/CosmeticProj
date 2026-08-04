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
import com.dtp.cosmemgt.sales.payment.dto.request.PaymentFailedEvent;
import com.dtp.cosmemgt.sales.payment.service.PaymentService;
import com.dtp.cosmemgt.warehouse.enums.TransactionTypeEnum;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
@Slf4j
public class CancelOrderService {
    CurrentUserService currentUserService;
    PaymentService paymentService;
    MailService mailService;
    ReturnOrderService returnOrderService;

    OrderRepository orderRepository;

    public void cancelOrder(String orderId) throws Exception {
        User u = currentUserService.getCurrentUser();
        Order order = getValidOwnedOrder(u, orderId);

        if (order.getOrderStatus() != OrderStatusEnum.PENDING && order.getOrderStatus() != OrderStatusEnum.CONFIRMED) {
            throw new AppException(ErrorCode.CAN_NOT_CANCEL_ORDER);
        }

        if (isEligibleForRefund(order)) {
            paymentService.refund(order);
            order.getInvoice().setPaymentStatus(PaymentStatusEnum.REFUNDED);
            mailService.sendOrderRefundEmail(u, order);
        }

        returnOrderService.processInventoryRestoration(order, TransactionTypeEnum.CANCEL_ORDER, false);
        order.setOrderStatus(OrderStatusEnum.CANCELLED);
        order.getInvoice().setPaymentStatus(PaymentStatusEnum.CANCELLED);   //ok
        log.info("Order [{}] cancelled successfully", orderId);
    }

    public void cancelOrderDueToPaymentFailure(String orderId, boolean isFromPaymentFailedEvent) {
        Order o = orderRepository.findById(orderId)
                .orElseThrow(() ->  new AppException(ErrorCode.ORDER_NOT_FOUND));

        if(o.getOrderStatus() != OrderStatusEnum.PENDING) {
            return;
        }

        o.setOrderStatus(OrderStatusEnum.CANCELLED);

        if(isFromPaymentFailedEvent) {
            o.getInvoice().setPaymentStatus(PaymentStatusEnum.CANCELLED);
        } else {
            o.getInvoice().setPaymentStatus(PaymentStatusEnum.FAILED);
        }

        returnOrderService.processInventoryRestoration(o, TransactionTypeEnum.CANCEL_ORDER, false);
    }

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

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)                  //tu tao transaction moi
    public void handlePaymentFailedEvent(PaymentFailedEvent event) {
        String orderId = event.getOrderId();
        log.info("Received PaymentFailedEvent for Order ID: {}", orderId);
        cancelOrderDueToPaymentFailure(orderId, true);
    }
}
