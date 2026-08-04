package com.dtp.cosmemgt.sales.order.service.command;

import com.dtp.cosmemgt.admin.entity.User;
import com.dtp.cosmemgt.core.commonService.CurrentUserService;
import com.dtp.cosmemgt.core.commonService.MailService;
import com.dtp.cosmemgt.core.exception.AppException;
import com.dtp.cosmemgt.core.exception.ErrorCode;
import com.dtp.cosmemgt.sales.order.entity.Order;
import com.dtp.cosmemgt.sales.order.enums.OrderStatusEnum;
import com.dtp.cosmemgt.sales.order.enums.PaymentStatusEnum;
import com.dtp.cosmemgt.sales.order.repository.OrderRepository;
import com.dtp.cosmemgt.sales.payment.service.PaymentService;
import com.dtp.cosmemgt.warehouse.entity.InventoryBatch;
import com.dtp.cosmemgt.warehouse.entity.InventoryTransaction;
import com.dtp.cosmemgt.warehouse.enums.TransactionTypeEnum;
import com.dtp.cosmemgt.warehouse.repository.InventoryTransactionRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
@Slf4j
public class ReturnOrderService {
    InventoryTransactionRepository inventoryTransactionRepository;
    CurrentUserService currentUserService;
    OrderRepository orderRepository;

    public void returnOrder(String orderId) throws Exception {
        User u = currentUserService.getCurrentUser();
        Order order = getValidOwnedOrder(u, orderId);

        if (order.getOrderStatus() != OrderStatusEnum.COMPLETED) {
            throw new AppException(ErrorCode.CAN_NOT_RETURN_ORDER);
        }

        LocalDateTime completedAt = order.getUpdatedAt();
        if (completedAt == null || completedAt.plusDays(7).isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.RETURN_PERIOD_EXPIRED);
        }

        order.setOrderStatus(OrderStatusEnum.RETURN_REQUESTED);
        log.info("Order [{}] return request submitted. Waiting for warehouse confirmation.", orderId);
    }

    //helpers
    private boolean isEligibleForRefund(Order order) {
        return order.getInvoice() != null && order.getInvoice().getPaymentStatus() == PaymentStatusEnum.PAID;
    }

    private Order getValidOwnedOrder(User currentUser, String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (order.getCustomer() == null || !order.getCustomer().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.ORDER_DO_NOT_BELONG);
        }

        return order;
    }

    public void processInventoryRestoration(Order order, TransactionTypeEnum transactionType, boolean isPhysicalReturn) {
        List<InventoryTransaction> trans = inventoryTransactionRepository.findAllByReferenceId(order.getId());

        List<InventoryTransaction> newTransToSave = trans.stream()
                .filter(tran -> tran.getChangeQty() < 0)
                .map(tran -> {
                    InventoryBatch b = tran.getInventoryBatch();
                    int refundQty = Math.abs(tran.getChangeQty());

                    b.setAvailableQty(b.getAvailableQty() + refundQty);
                    if (isPhysicalReturn) {
                        b.setPhysicalQty(b.getPhysicalQty() + refundQty);
                    }

                    return InventoryTransaction.builder()
                            .changeQty(refundQty)
                            .inventoryBatch(b)
                            .referenceId(order.getId())
                            .transactionType(transactionType)
                            .build();
                })
                .toList();

        inventoryTransactionRepository.saveAll(newTransToSave);
    }
}
