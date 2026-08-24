package com.dtp.cosmemgt.sales.order.service.command;

import com.dtp.cosmemgt.core.commonService.MailService;
import com.dtp.cosmemgt.core.exception.AppException;
import com.dtp.cosmemgt.core.exception.ErrorCode;
import com.dtp.cosmemgt.sales.order.entity.Order;
import com.dtp.cosmemgt.sales.order.enums.OrderStatusEnum;
import com.dtp.cosmemgt.sales.order.enums.PaymentStatusEnum;
import com.dtp.cosmemgt.sales.order.repository.OrderRepository;
import com.dtp.cosmemgt.sales.payment.service.PaymentService;
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
public class RefundOrderService {
    OrderRepository orderRepository;

    PaymentService paymentService;
    MailService mailService;

    public void adminManualConfirmRefund(String orderId) throws Exception {
        Order o = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if(o.getOrderStatus() != OrderStatusEnum.RETURNED ||
                o.getInvoice().getPaymentStatus() != PaymentStatusEnum.PENDING_REFUND) {
            throw new AppException(ErrorCode.INVALID_REFUND_CONDITION);
        }

        paymentService.refund(o);

        mailService.sendOrderRefundEmail(o.getCustomer(), o);

        log.info("Admin successfully processed refund for order [{}]", orderId);
    }
}
