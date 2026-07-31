package com.dtp.cosmemgt.sales.customer.service;

import com.dtp.cosmemgt.admin.entity.User;
import com.dtp.cosmemgt.admin.repository.UserRepository;
import com.dtp.cosmemgt.catalog.entity.ProductVariant;
import com.dtp.cosmemgt.catalog.repository.ProductVariantRepository;
import com.dtp.cosmemgt.catalog.service.specification.OrderSpecification;
import com.dtp.cosmemgt.core.dto.PageResponse;
import com.dtp.cosmemgt.core.exception.AppException;
import com.dtp.cosmemgt.core.exception.ErrorCode;
import com.dtp.cosmemgt.core.commonService.MailService;
import com.dtp.cosmemgt.sales.customer.dto.request.OrderCreationRequest;
import com.dtp.cosmemgt.sales.customer.dto.request.OrderDetailRequest;
import com.dtp.cosmemgt.sales.customer.dto.response.OrderDetailResponse;
import com.dtp.cosmemgt.sales.customer.dto.response.OrderResponse;
import com.dtp.cosmemgt.sales.customer.mapper.OrderMapper;
import com.dtp.cosmemgt.sales.entity.Invoice;
import com.dtp.cosmemgt.sales.entity.Order;
import com.dtp.cosmemgt.sales.entity.OrderDetail;
import com.dtp.cosmemgt.sales.enums.OrderStatusEnum;
import com.dtp.cosmemgt.sales.enums.PaymentMethodEnum;
import com.dtp.cosmemgt.sales.enums.PaymentStatusEnum;
import com.dtp.cosmemgt.sales.repository.InvoiceRepository;
import com.dtp.cosmemgt.sales.repository.OrderRepository;
import com.dtp.cosmemgt.warehouse.entity.InventoryBatch;
import com.dtp.cosmemgt.warehouse.entity.InventoryTransaction;
import com.dtp.cosmemgt.warehouse.enums.TransactionTypeEnum;
import com.dtp.cosmemgt.warehouse.repository.InventoryBatchRepository;
import com.dtp.cosmemgt.warehouse.repository.InventoryTransactionRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
@Slf4j
public class OrderService {

    ProductVariantRepository productVariantRepository;
    UserRepository userRepository;
    OrderRepository orderRepository;
    InventoryBatchRepository inventoryBatchRepository;
    InventoryTransactionRepository inventoryTransactionRepository;
    InvoiceRepository invoiceRepository;

    MailService mailService;
    PaymentService paymentService;

    OrderMapper orderMapper;

    public OrderResponse create(OrderCreationRequest request) {
        User currentUser = getCurrentUser();

        Order order = Order.builder()
                .customer(currentUser)
                .orderStatus(OrderStatusEnum.PENDING)
                .build();

        //FIFO
        List<InventoryTransaction> transactionsToSave = processOrderItemsAndInventory(request, order);

        Order savedOrder = orderRepository.save(order);

        createInvoiceForOrder(savedOrder, request.getPaymentMethod());

        transactionsToSave.forEach(tx -> tx.setReferenceId(savedOrder.getId()));
        inventoryTransactionRepository.saveAll(transactionsToSave);



        log.info("Order [{}] created successfully for user [{}]", savedOrder.getId(), currentUser.getId());
        return orderMapper.toOrderResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrderDetailResponse getOrderDetail(String orderId) {
        User u = getCurrentUser();
        Order order = getValidOwnedOrder(u, orderId);
        return orderMapper.toOrderDetailResponse(order);
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getAllMyOrder(Map<String, String> queryParams) {
        User myAccount = getCurrentUser();

        int page = queryParams.containsKey("page") ? Integer.parseInt(queryParams.get("page")) : 0;
        int size = queryParams.containsKey("size") ? Integer.parseInt(queryParams.get("size")) : 10;
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Specification<Order> filterOrderSpec = OrderSpecification.filterOrder(queryParams);
        Specification<Order> customerSpec = (root, query, cb) -> cb.equal(root.get("customer"), myAccount);

        Page<Order> orderPage = orderRepository.findAll(Specification.where(customerSpec).and(filterOrderSpec), pageable);

        return PageResponse.of(orderPage.map(orderMapper::toOrderResponse));
    }

    public void cancelOrder(String orderId) throws Exception {
        User u = this.getCurrentUser();
        Order order = getValidOwnedOrder(u, orderId);

        if (order.getOrderStatus() != OrderStatusEnum.PENDING && order.getOrderStatus() != OrderStatusEnum.CONFIRMED) {
            throw new AppException(ErrorCode.CAN_NOT_CANCEL_ORDER);
        }

        if (isEligibleForRefund(order)) {
            paymentService.refund(order);
            order.getInvoice().setPaymentStatus(PaymentStatusEnum.REFUNDED);
            sendOrderRefundEmail(u, order);
        }

        processInventoryRestoration(order, TransactionTypeEnum.CANCEL_ORDER, false);
        order.setOrderStatus(OrderStatusEnum.CANCELLED);
        log.info("Order [{}] cancelled successfully", orderId);
    }

    public void returnOrder(String orderId) throws Exception {
        User u = this.getCurrentUser();
        Order order = getValidOwnedOrder(u, orderId);

        if (order.getOrderStatus() != OrderStatusEnum.COMPLETED) {
            throw new AppException(ErrorCode.CAN_NOT_RETURN_ORDER);
        }

        LocalDateTime completedAt = order.getUpdatedAt();
        if (completedAt == null || completedAt.plusDays(7).isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.RETURN_PERIOD_EXPIRED);
        }

        // KHÔNG hoàn tiền và KHÔNG cộng kho ở đây.
        // Chỉ đổi trạng thái sang chờ kho xử lý.
        order.setOrderStatus(OrderStatusEnum.RETURN_REQUESTED);
        log.info("Order [{}] return request submitted. Waiting for warehouse confirmation.", orderId);
    }

    //hoan kho khi thanh toan FAILED
    public void cancelOrderDueToPaymentFailure(String orderId) {
        Order o = orderRepository.findById(orderId)
                .orElseThrow(() ->  new AppException(ErrorCode.ORDER_NOT_FOUND));

        if(o.getOrderStatus() != OrderStatusEnum.PENDING) {
            return;
        }

        o.setOrderStatus(OrderStatusEnum.CANCELLED);

        this.processInventoryRestoration(o, TransactionTypeEnum.CANCEL_ORDER, false);
    }


    //HELPERS METHODS
    private User getCurrentUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        String userId = authentication.getName();
        if (userId == null || userId.isBlank()) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        return userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    private Order getValidOwnedOrder(User currentUser, String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (order.getCustomer() == null || !order.getCustomer().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.ORDER_DO_NOT_BELONG);
        }

        return order;
    }

    private void createInvoiceForOrder(Order order, String paymentMethodStr) {
        Invoice invoice = Invoice.builder()
                .order(order)
                .amount(order.getTotalAmount())
                .paymentMethod(PaymentMethodEnum.valueOf(paymentMethodStr))
                .paymentStatus(PaymentStatusEnum.UNPAID)
                .build();
        invoiceRepository.save(invoice);
    }

    private boolean isEligibleForRefund(Order order) {
        return order.getInvoice() != null && order.getInvoice().getPaymentStatus() == PaymentStatusEnum.PAID;
    }

    private List<InventoryTransaction> processOrderItemsAndInventory(OrderCreationRequest request, Order order) {
        BigDecimal orderTotalAmount = BigDecimal.ZERO;
        BigDecimal orderTotalCogs = BigDecimal.ZERO;

        List<OrderDetail> ods = new ArrayList<>();
        List<InventoryTransaction> transactionsToSave = new ArrayList<>();

        for (OrderDetailRequest odRequest : request.getOrderDetailRequests()) {
            String variantId = odRequest.getProductVariantId();
            int requireQty = odRequest.getQty();

            //FIFO
            List<InventoryBatch> availableBatches = inventoryBatchRepository.findAllAvailableBatchesFIFO(variantId);

            int actualTotalStock = availableBatches.stream().mapToInt(InventoryBatch::getAvailableQty).sum();
            if (actualTotalStock < requireQty) {
                throw new AppException(ErrorCode.OUT_OF_STOCK);
            }

            //tru kho tam giu va tong COGS (tong phi san xuat )
            BigDecimal lineTotalCogs = reserveStockAndCalculateCogs(availableBatches, requireQty, transactionsToSave);

            ProductVariant variant = productVariantRepository.findById(variantId)
                    .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_VARIANT_NOT_EXISTED));

            BigDecimal unitCogs = lineTotalCogs.divide(BigDecimal.valueOf(requireQty), 4, RoundingMode.HALF_UP);
            BigDecimal purchasedPrice = variant.getProduct().getBasePrice();

            OrderDetail od = OrderDetail.builder()
                    .productVariant(variant)
                    .order(order)
                    .quantity(requireQty)
                    .purchasedPrice(purchasedPrice)
                    .unitCogs(unitCogs)
                    .build();

            ods.add(od);

            orderTotalAmount = orderTotalAmount.add(purchasedPrice.multiply(BigDecimal.valueOf(requireQty)));
            orderTotalCogs = orderTotalCogs.add(lineTotalCogs);
        }

        order.setTotalAmount(orderTotalAmount);
        order.setTotalCogs(orderTotalCogs);
        order.setOrderDetails(ods);

        return transactionsToSave;
    }

    private BigDecimal reserveStockAndCalculateCogs(List<InventoryBatch> batches, int requireQty, List<InventoryTransaction> transactionsToSave) {
        BigDecimal lineTotalCogs = BigDecimal.ZERO;
        int remainingToFulfill = requireQty;

        for (InventoryBatch batch : batches) {
            if (remainingToFulfill == 0) break;

            int qtyToTake = Math.min(batch.getAvailableQty(), remainingToFulfill);
            batch.setAvailableQty(batch.getAvailableQty() - qtyToTake);

            BigDecimal costFromThisBatch = batch.getUnitCost().multiply(BigDecimal.valueOf(qtyToTake));
            lineTotalCogs = lineTotalCogs.add(costFromThisBatch);

            InventoryTransaction transaction = InventoryTransaction.builder()
                    .inventoryBatch(batch)
                    .changeQty(-qtyToTake)
                    .transactionType(TransactionTypeEnum.RESERVE)
                    .build();

            transactionsToSave.add(transaction);
            remainingToFulfill -= qtyToTake;
        }

        return lineTotalCogs;
    }

    private void processInventoryRestoration(Order order, TransactionTypeEnum transactionType, boolean isPhysicalReturn) {
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

    public void sendOrderRefundEmail(User user, Order order) {
        String subject = "Hoàn tiền đơn hàng - Mã đơn #" + order.getId();
        String htmlBody = String.format("""
            <div style="font-family: Arial, sans-serif; line-height: 1.6;">
                <h2>Xin chào %s,</h2>
                <p>Cảm ơn bạn đã đặt hàng tại <b>CosmeMgt</b>. Đơn hàng của bạn đã được hoàn tiền thành công!</p>
                <ul>
                    <li><b>Mã đơn hàng:</b> %s</li>
                    <li><b>Tổng tiền:</b> %,d VNĐ</li>
                    <li><b>Trạng thái:</b> Đã hoàn tiền</li>
                </ul>
                <p>Cảm ơn bạn đã đặt hàng. Mọi thắc mắc liên hệ 1900....!</p>
            </div>
            """, user.getFullName(), order.getId(), order.getTotalAmount().longValue());

        mailService.sendEmail(user.getEmail(), user.getFullName(), subject, htmlBody);
    }
}