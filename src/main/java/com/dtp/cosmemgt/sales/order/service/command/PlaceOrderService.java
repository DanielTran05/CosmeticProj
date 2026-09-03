package com.dtp.cosmemgt.sales.order.service.command;

import com.dtp.cosmemgt.admin.entity.User;
import com.dtp.cosmemgt.catalog.entity.ProductVariant;
import com.dtp.cosmemgt.catalog.repository.ProductVariantRepository;
import com.dtp.cosmemgt.core.commonService.CurrentUserService;
import com.dtp.cosmemgt.core.exception.AppException;
import com.dtp.cosmemgt.core.exception.ErrorCode;
import com.dtp.cosmemgt.sales.order.dto.request.CartCalculateRequest;
import com.dtp.cosmemgt.sales.order.dto.request.OrderCreationRequest;
import com.dtp.cosmemgt.sales.order.dto.request.OrderDetailRequest;
import com.dtp.cosmemgt.sales.order.dto.request.ShippingOrderCreationRequest;
import com.dtp.cosmemgt.sales.order.dto.response.CartCalculateResponse;
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
import com.dtp.cosmemgt.sales.promotion.service.OrderPromotionUsageService;
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
    OrderPromotionUsageService orderPromotionUsageService;
    CartCalculationService cartCalculationService;

    OrderRepository orderRepository;

    OrderMapper orderMapper;

    public OrderResponse create(OrderCreationRequest request) {
        User currentUser = currentUserService.getCurrentUser();

        // create order
        Order order = Order.builder()
                .customer(currentUser)
                .orderStatus(OrderStatusEnum.PENDING)
                .build();
        Order savedOrder = orderRepository.save(order);

        // re-calculate voucher and total amount
        CartCalculateRequest cartCalculateRequest = CartCalculateRequest.builder()
                .items(request.getOrderDetailRequests())
                .voucherCode(request.getCodeVoucher())
                .build();
        CartCalculateResponse cartCalculateResponse = cartCalculationService.calculate(cartCalculateRequest);

        if (cartCalculateResponse.getErrorMsg() != null) {
            throw new AppException(ErrorCode.INVALID_VOUCHER);
        }

        List<InventoryTransaction> transactionsToSave = reservationService.reserveInventory(request, order);
        reservationService.persistReservedTransactions(transactionsToSave, savedOrder);

        //save discount info
        savedOrder.setTotalAmount(cartCalculateResponse.getFinalTotal());
        savedOrder.setDiscountAmount(cartCalculateResponse.getVoucherDiscount());
        savedOrder.setVoucherCode(cartCalculateResponse.getAppliedVoucherCode());
        orderRepository.save(savedOrder);

        //deduct promotions limit for Product/Variant
        List<ProductVariant> purchasedVariants = savedOrder.getOrderDetails().stream()
                .map(OrderDetail::getProductVariant)
                .toList();
        orderPromotionUsageService.deductProductPromotions(purchasedVariants);

        //deduct promotion limit for Order
        if (cartCalculateResponse.getAppliedVoucherCode() != null) {
            orderPromotionUsageService.deductOrderVoucher(cartCalculateResponse.getAppliedVoucherCode());
        }

        //create invoice and shipping info
        invoiceService.createInvoiceForOrder(savedOrder, request.getPaymentMethod());
        orderShippingService.createOrderShipping(savedOrder, currentUser, request);

        log.info("Order [{}] created successfully for user [{}]", savedOrder.getId(), currentUser.getId());
        return orderMapper.toOrderResponse(savedOrder);
    }
}