package com.dtp.cosmemgt.sales.payment.service;

import com.dtp.cosmemgt.sales.order.entity.Order;
import com.dtp.cosmemgt.sales.payment.dto.request.PaymentCreationRequest;
import com.dtp.cosmemgt.sales.payment.dto.response.PaymentStatusResponse;
import com.dtp.cosmemgt.sales.payment.dto.response.PaymentResponse;

public interface IPaymentService {
    PaymentResponse createPaymentRequest(PaymentCreationRequest paymentCreationRequest, String ipAddress) throws Exception;

    void refund(Order order) throws Exception;

    PaymentStatusResponse checkTransactionStatus(Order order) throws Exception;
    void rescueMissedPayment(Order order, String transId);
}
