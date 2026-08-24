package com.dtp.cosmemgt.sales.payment.service;

import com.dtp.cosmemgt.core.commonService.MailService;
import com.dtp.cosmemgt.core.exception.AppException;
import com.dtp.cosmemgt.core.exception.ErrorCode;
import com.dtp.cosmemgt.sales.order.entity.Order;
import com.dtp.cosmemgt.sales.order.enums.PaymentStatusEnum;
import com.dtp.cosmemgt.sales.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RefundProcessor {
    OrderRepository orderRepository;
    PaymentService paymentService;
    MailService mailService;

    @Transactional(rollbackFor = Exception.class)
    public void executeSingleRefund(String orderId) throws Exception {
        Order o = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        
        paymentService.refund(o);
        
        mailService.sendOrderRefundEmail(o.getCustomer(), o);
    }
}