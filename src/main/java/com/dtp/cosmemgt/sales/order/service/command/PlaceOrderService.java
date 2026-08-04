package com.dtp.cosmemgt.sales.order.service.command;

import com.dtp.cosmemgt.admin.entity.User;
import com.dtp.cosmemgt.catalog.entity.ProductVariant;
import com.dtp.cosmemgt.catalog.repository.ProductVariantRepository;
import com.dtp.cosmemgt.core.commonService.CurrentUserService;
import com.dtp.cosmemgt.core.exception.AppException;
import com.dtp.cosmemgt.core.exception.ErrorCode;
import com.dtp.cosmemgt.sales.order.dto.request.OrderCreationRequest;
import com.dtp.cosmemgt.sales.order.dto.request.OrderDetailRequest;
import com.dtp.cosmemgt.sales.order.dto.request.ShippingOrderCreationRequest;
import com.dtp.cosmemgt.sales.order.dto.response.OrderResponse;
import com.dtp.cosmemgt.sales.order.entity.Order;
import com.dtp.cosmemgt.sales.order.entity.OrderDetail;
import com.dtp.cosmemgt.sales.order.entity.OrderShipping;
import com.dtp.cosmemgt.sales.order.enums.OrderStatusEnum;
import com.dtp.cosmemgt.sales.order.enums.PaymentMethodEnum;
import com.dtp.cosmemgt.sales.order.enums.PaymentStatusEnum;
import com.dtp.cosmemgt.sales.order.mapper.OrderMapper;
import com.dtp.cosmemgt.sales.order.repository.InvoiceRepository;
import com.dtp.cosmemgt.sales.order.repository.OrderRepository;
import com.dtp.cosmemgt.sales.order.repository.OrderShippingRepository;
import com.dtp.cosmemgt.sales.payment.entity.Invoice;
import com.dtp.cosmemgt.warehouse.entity.InventoryBatch;
import com.dtp.cosmemgt.warehouse.entity.InventoryTransaction;
import com.dtp.cosmemgt.warehouse.enums.TransactionTypeEnum;
import com.dtp.cosmemgt.warehouse.repository.InventoryBatchRepository;
import com.dtp.cosmemgt.warehouse.repository.InventoryTransactionRepository;
import com.dtp.cosmemgt.warehouse.service.WarehouseReservationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
@Slf4j
public class PlaceOrderService {
    CurrentUserService currentUserService;
    WarehouseReservationService reservationService;
    OrderShippingService orderShippingService;
    InvoiceService invoiceService;

    OrderRepository orderRepository;

    OrderMapper orderMapper;

    public OrderResponse create(OrderCreationRequest request) {
        User currentUser = currentUserService.getCurrentUser();

        Order order = Order.builder()
                .customer(currentUser)
                .orderStatus(OrderStatusEnum.PENDING)
                .build();

        List<InventoryTransaction> transactionsToSave = reservationService.reserveInventory(request, order);
        Order savedOrder = orderRepository.save(order);

        invoiceService.createInvoiceForOrder(savedOrder, request.getPaymentMethod());

        orderShippingService.createOrderShipping(savedOrder, currentUser, request);

        reservationService.persistReservedTransactions(transactionsToSave, savedOrder);

        log.info("Order [{}] created successfully for user [{}]", savedOrder.getId(), currentUser.getId());
        return orderMapper.toOrderResponse(savedOrder);
    }
}
