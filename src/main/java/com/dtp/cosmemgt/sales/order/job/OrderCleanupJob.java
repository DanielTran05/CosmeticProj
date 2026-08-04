package com.dtp.cosmemgt.sales.order.job;

import com.dtp.cosmemgt.sales.order.service.command.CancelOrderService;
import com.dtp.cosmemgt.sales.order.entity.Order;
import com.dtp.cosmemgt.sales.order.enums.OrderStatusEnum;
import com.dtp.cosmemgt.sales.order.repository.OrderRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class OrderCleanupJob {

    OrderRepository orderRepository;
    CancelOrderService cancelOrderService;

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void cleanupExpiredPendingOrders() {
        LocalDateTime expirationTime = LocalDateTime.now().minusMinutes(30);

        List<Order> expiredOrders = orderRepository.findByOrderStatusAndCreatedAtBefore(
                OrderStatusEnum.PENDING,
                expirationTime
        );

        if (!expiredOrders.isEmpty()) {
            log.info("Found {} expired pending orders. Starting cancellation process...", expiredOrders.size());

            for (Order order : expiredOrders) {
                try {
                    cancelOrderService.cancelOrderDueToPaymentFailure(order.getId(), false);
                    log.info("Successfully cancelled expired OrderId: {}", order.getId());
                } catch (Exception e) {
                    log.error("Failed to cancel expired OrderId: {}", order.getId(), e);
                }
            }
        }
    }
}