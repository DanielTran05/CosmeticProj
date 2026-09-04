package com.dtp.cosmemgt.sales.order.job;

import com.dtp.cosmemgt.sales.order.service.command.CancelOrderService;
import com.dtp.cosmemgt.sales.order.entity.Order;
import com.dtp.cosmemgt.sales.order.enums.OrderStatusEnum;
import com.dtp.cosmemgt.sales.order.enums.PaymentStatusEnum;
import com.dtp.cosmemgt.sales.order.repository.OrderRepository;
import com.dtp.cosmemgt.sales.payment.dto.response.PaymentStatusResponse;
import com.dtp.cosmemgt.sales.payment.service.IPaymentService; // Đã đổi import

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class OrderCleanupJob {

    OrderRepository orderRepository;
    CancelOrderService cancelOrderService;
    Map<String, IPaymentService> paymentServiceMap;

    @Scheduled(fixedRate = 60000)
    public void cleanupExpiredPendingOrders() {
        LocalDateTime expirationTime = LocalDateTime.now().minusMinutes(30);

        List<Order> expiredOrders = orderRepository.findByOrderStatusAndCreatedAtBefore(
                OrderStatusEnum.PENDING,
                expirationTime
        );

        if (expiredOrders.isEmpty()) {
            return;
        }

        log.info("Found {} order timeout, check Payment status before cancel...", expiredOrders.size());

        for (Order order : expiredOrders) {
            try {
                if (order.getInvoice() != null && order.getInvoice().getPaymentStatus() == PaymentStatusEnum.PENDING) {

                    String paymentMethod = order.getInvoice().getPaymentMethod().name();
                    IPaymentService paymentService = paymentServiceMap.get(paymentMethod);

                    if (paymentService != null) {
                        PaymentStatusResponse status = paymentService.checkTransactionStatus(order);

                        if (status.isPaid()) {
                            paymentService.rescueMissedPayment(order, status.getTransId());
                            continue;
                        }
                    }
                }

                cancelOrderService.cancelOrderDueToPaymentFailure(order.getId(), false);
                log.info("Cancelled successfully order timeout for OrderID: {}", order.getId());

            } catch (Exception e) {
                log.error("Error occurred when cleanup OrderTimeout: {}", order.getId(), e);
            }
        }
    }
}