package com.dtp.cosmemgt.sales.order.service.command;

import com.dtp.cosmemgt.core.commonService.MailService;
import com.dtp.cosmemgt.core.exception.AppException;
import com.dtp.cosmemgt.core.exception.ErrorCode;
import com.dtp.cosmemgt.sales.order.entity.Order;
import com.dtp.cosmemgt.sales.order.enums.OrderStatusEnum;
import com.dtp.cosmemgt.sales.order.enums.PaymentMethodEnum;
import com.dtp.cosmemgt.sales.order.enums.PaymentStatusEnum;
import com.dtp.cosmemgt.sales.order.repository.OrderRepository;
import com.dtp.cosmemgt.sales.payment.service.IPaymentService;
import com.dtp.cosmemgt.sales.payment.service.impl.MomoPaymentService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
@Slf4j
public class RefundOrderService {
    OrderRepository orderRepository;

    MailService mailService;
    Map<String, IPaymentService> paymentServiceMap;

    public void adminManualConfirmRefund(String orderId) throws Exception {
        Order o = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if(o.getOrderStatus() != OrderStatusEnum.RETURNED ||
                o.getInvoice().getPaymentStatus() != PaymentStatusEnum.PENDING_REFUND) {
            throw new AppException(ErrorCode.INVALID_REFUND_CONDITION);
        }

        IPaymentService paymentService = paymentServiceMap.get(o.getInvoice().getPaymentMethod().name());

        if (paymentService == null) {
            throw new AppException(ErrorCode.PAYMENT_METHOD_NOT_SUPPORTED);
        }else{
            paymentService.refund(o);
        }

        if (o.getInvoice().getPaymentMethod() == PaymentMethodEnum.VNPAY)
            mailService.sendWaitingOrderRefundEmail(o.getCustomer(), o);
        else
              mailService.sendOrderRefundEmail(o.getCustomer(), o);

        log.info("Admin successfully processed refund for order [{}]", orderId);
    }
}
