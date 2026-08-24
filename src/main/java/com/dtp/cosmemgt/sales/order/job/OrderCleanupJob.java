package com.dtp.cosmemgt.sales.order.job;

import com.dtp.cosmemgt.sales.order.service.command.CancelOrderService;
import com.dtp.cosmemgt.sales.order.entity.Order;
import com.dtp.cosmemgt.sales.order.enums.OrderStatusEnum;
import com.dtp.cosmemgt.sales.order.enums.PaymentStatusEnum; // Thêm dòng này
import com.dtp.cosmemgt.sales.order.repository.OrderRepository;
import com.dtp.cosmemgt.sales.payment.dto.response.MoMoStatusResponse; // Import class bạn tạo ở bước 3 trước đó
import com.dtp.cosmemgt.sales.payment.service.PaymentService; // Thêm dòng này

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class OrderCleanupJob {

    OrderRepository orderRepository;
    CancelOrderService cancelOrderService;
    PaymentService paymentService;

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

        log.info("Found {} expired pending orders. Checking MoMo status before cancellation...", expiredOrders.size());

        for (Order order : expiredOrders) {
            try {
                if (order.getInvoice() != null && order.getInvoice().getPaymentStatus() == PaymentStatusEnum.PENDING) {

                    MoMoStatusResponse status = paymentService.checkMoMoTransactionStatus(order);

                    if (status.isPaid()) {
                        paymentService.rescueMissedPayment(order, status.getTransId());
                        continue;
                    }
                }

                cancelOrderService.cancelOrderDueToPaymentFailure(order.getId(), false);
                log.info("Successfully cancelled expired OrderId: {}", order.getId());

            } catch (Exception e) {
                log.error("Failed to process expired OrderId: {}", order.getId(), e);
            }
        }
    }
}