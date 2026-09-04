package com.dtp.cosmemgt.sales.order.service.command;

import com.dtp.cosmemgt.sales.order.entity.Order;
import com.dtp.cosmemgt.sales.order.enums.PaymentMethodEnum;
import com.dtp.cosmemgt.sales.order.enums.PaymentStatusEnum;
import com.dtp.cosmemgt.sales.payment.repository.InvoiceRepository;
import com.dtp.cosmemgt.sales.payment.entity.Invoice;
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
public class InvoiceService {
    InvoiceRepository invoiceRepository;

    public void createInvoiceForOrder(Order order, String paymentMethodStr) {
        Invoice invoice = Invoice.builder()
                .order(order)
                .amount(order.getTotalAmount())
                .paymentMethod(PaymentMethodEnum.valueOf(paymentMethodStr))
                .paymentStatus(PaymentStatusEnum.UNPAID)
                .build();
        invoiceRepository.save(invoice);
    }
}
