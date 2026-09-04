package com.dtp.cosmemgt.sales.payment.service;

import com.dtp.cosmemgt.core.exception.AppException;
import com.dtp.cosmemgt.core.commonService.MailService;
import com.dtp.cosmemgt.core.exception.ErrorCode;
import com.dtp.cosmemgt.sales.order.entity.Order;
import com.dtp.cosmemgt.sales.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class RefundProcessor {
    private final OrderRepository orderRepository;
    private final MailService mailService;
    private final Map<String, IPaymentService> paymentServiceMap;

    @Transactional(rollbackFor = Exception.class)
    public void executeSingleRefund(String orderId) throws Exception {
        Order o = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        
        String paymentMethod = o.getInvoice().getPaymentMethod().name();
        
        IPaymentService paymentService = paymentServiceMap.get(paymentMethod);
        if (paymentService == null) {
            throw new AppException(ErrorCode.PAYMENT_METHOD_NOT_SUPPORTED);
        }
        
        paymentService.refund(o);
        
        mailService.sendOrderRefundEmail(o.getCustomer(), o);
    }
}