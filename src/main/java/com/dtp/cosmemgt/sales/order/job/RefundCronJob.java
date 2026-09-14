package com.dtp.cosmemgt.sales.order.job;

import com.dtp.cosmemgt.sales.order.entity.Order;
import com.dtp.cosmemgt.sales.order.enums.OrderStatusEnum;
import com.dtp.cosmemgt.sales.order.enums.PaymentStatusEnum;
import com.dtp.cosmemgt.sales.order.repository.OrderRepository;
import com.dtp.cosmemgt.sales.payment.service.RefundProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class RefundCronJob {

    private final OrderRepository orderRepository;
    private final RefundProcessor refundProcessor;

    @Scheduled(cron = "0 0/30 * * * ?")
    public void processAutoRefunds() {
        log.info("Start job for automatically refund for cancelled orders...");

        List<Order> autoRefundOrders = orderRepository.findByOrderStatusAndInvoice_PaymentStatus(
                OrderStatusEnum.CANCELLED,
                PaymentStatusEnum.PENDING_REFUND
        );

        for (Order order : autoRefundOrders) {
            try {
                refundProcessor.executeSingleRefund(order.getId());
            } catch (Exception e) {
                log.error("Error refund for order {}: {}", order.getId(), e.getMessage());
            }
        }
    }
}