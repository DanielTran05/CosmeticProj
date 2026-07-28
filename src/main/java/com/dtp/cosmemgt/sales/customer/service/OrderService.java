package com.dtp.cosmemgt.sales.customer.service;

import com.dtp.cosmemgt.admin.entity.User;
import com.dtp.cosmemgt.admin.repository.UserRepository;
import com.dtp.cosmemgt.catalog.entity.ProductVariant;
import com.dtp.cosmemgt.catalog.repository.ProductVariantRepository;
import com.dtp.cosmemgt.catalog.service.specification.OrderSpecification;
import com.dtp.cosmemgt.core.exception.AppException;
import com.dtp.cosmemgt.core.exception.ErrorCode;
import com.dtp.cosmemgt.sales.customer.dto.request.OrderCreationRequest;
import com.dtp.cosmemgt.sales.customer.dto.request.OrderDetailRequest;
import com.dtp.cosmemgt.sales.customer.dto.response.OrderDetailResponse;
import com.dtp.cosmemgt.sales.customer.dto.response.OrderResponse;
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
import com.dtp.cosmemgt.sales.customer.mapper.OrderMapper;
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

    OrderMapper orderMapper;

    public OrderResponse create(OrderCreationRequest request) {
        User currentUser = this.getCurrentUser();

        Order order = Order.builder()
                .customer(currentUser)
                .orderStatus(OrderStatusEnum.PENDING)
                .build();

        // tru kho, tinh gia von, chi tiet don hang
        List<InventoryTransaction> transactionToSave = processOrderItemsAndInventory(request, order);

        Order savedOrder = orderRepository.save(order);

        Invoice invoice = Invoice.builder()
                .order(savedOrder)
                .amount(savedOrder.getTotalAmount())
                .paymentMethod(PaymentMethodEnum.valueOf(request.getPaymentMethod()))
                .paymentStatus(PaymentStatusEnum.UNPAID)
                .build();
        invoiceRepository.save(invoice);

        for (InventoryTransaction tx : transactionToSave) {
            tx.setReferenceId(savedOrder.getId());
        }
        inventoryTransactionRepository.saveAll(transactionToSave);

        return orderMapper.toOrderResponse(savedOrder);
    }

    public OrderDetailResponse getOrderDetail(String orderId){
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        User currentUser = this.getCurrentUser();

        if(order.getCustomer() == null || !order.getCustomer().getId().equals(currentUser.getId()))
            throw new AppException(ErrorCode.ORDER_DO_NOT_BELONG);

        return orderMapper.toOrderDetailResponse(order);
        }

    public Page<OrderResponse> getAllMyOrder(Map<String, String> queryParams){
            User myAccount = this.getCurrentUser();

            int page = queryParams.containsKey("page") ? Integer.parseInt(queryParams.get("page")) : 0;
            int size = queryParams.containsKey("size") ? Integer.parseInt(queryParams.get("size")) : 10;
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

            Specification<Order> filterOrderSpec = OrderSpecification.filterOrder(queryParams);

            Specification<Order> customerSpec = ((root, query, cb) ->
                    cb.equal(root.get("customer"), myAccount));

            Page<Order> orderPage = orderRepository.findAll(Specification.where(customerSpec).and(filterOrderSpec), pageable);

            return orderPage.map(orderMapper::toOrderResponse);
        }

    public void cancelOrder(String orderId) {
        Order order = getValidOwnedOrder(orderId);

        OrderStatusEnum status = order.getOrderStatus();
        if (status != OrderStatusEnum.PENDING && status != OrderStatusEnum.CONFIRMED) {
            throw new AppException(ErrorCode.CAN_NOT_CANCEL_ORDER);
        }

        //hoan tien
        if (status == OrderStatusEnum.CONFIRMED ||
                (order.getInvoice() != null && order.getInvoice().getPaymentStatus() == PaymentStatusEnum.PAID)) {
            // TODO: Xây dựng hàm gọi MoMo API hoàn tiền tại đây
            // momoPaymentService.refund(order.getId(), order.getTotalAmount());
            order.getInvoice().setPaymentStatus(PaymentStatusEnum.REFUNDED);
        }

        processInventoryRestoration(order, TransactionTypeEnum.CANCEL_ORDER, false);
        order.setOrderStatus(OrderStatusEnum.CANCELLED);
    }

    public void returnOrder(String orderId) {
        Order order = getValidOwnedOrder(orderId);

        if (order.getOrderStatus() != OrderStatusEnum.COMPLETED) {
            throw new AppException(ErrorCode.CAN_NOT_RETURN_ORDER);
        }

        // Trả hàng luôn đi kèm hoàn tiền
        if (order.getInvoice() != null && order.getInvoice().getPaymentStatus() == PaymentStatusEnum.PAID) {
            // TODO: Xây dựng hàm gọi MoMo API hoàn tiền tại đây
            order.getInvoice().setPaymentStatus(PaymentStatusEnum.REFUNDED);
        }

        //hoan hang
        processInventoryRestoration(order, TransactionTypeEnum.RETURN_ORDER, true);
        order.setOrderStatus(OrderStatusEnum.RETURNED);
    }





    //utils
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

    private List<InventoryTransaction> processOrderItemsAndInventory(
            OrderCreationRequest request, Order order) {
        BigDecimal orderTotalAmount = BigDecimal.ZERO;
        BigDecimal orderTotalCogs = BigDecimal.ZERO;
        List<OrderDetail> ods = new ArrayList<>();
        List<InventoryTransaction> transactionToSave = new ArrayList<>();

        for (OrderDetailRequest odRequest : request.getOrderDetailRequests()) {
            String variantId = odRequest.getProductVariantId();
            int requireQty = odRequest.getQty();

            List<InventoryBatch> availableBatches = inventoryBatchRepository.findAllAvailableBatchesFIFO(variantId);

            int actualTotalStock = availableBatches.stream()
                    .mapToInt(InventoryBatch::getAvailableQty)
                    .sum();

            if (actualTotalStock < requireQty) {
                throw new AppException(ErrorCode.OUT_OF_STOCK);
            }

            BigDecimal lineTotalCogs = BigDecimal.ZERO;
            int remainingToFulFill = requireQty;

            for (InventoryBatch b : availableBatches) {
                if (remainingToFulFill == 0) break;

                int qtyToTake = Math.min(b.getAvailableQty(), remainingToFulFill);

                b.setAvailableQty(b.getAvailableQty() - qtyToTake);

                BigDecimal costFromThisBatch = b.getUnitCost().multiply(BigDecimal.valueOf(qtyToTake));
                lineTotalCogs = lineTotalCogs.add(costFromThisBatch);

                InventoryTransaction transaction = InventoryTransaction.builder()
                        .inventoryBatch(b)
                        .changeQty(-qtyToTake)
                        .transactionType(TransactionTypeEnum.RESERVE)
                        .build();
                transactionToSave.add(transaction);

                remainingToFulFill -= qtyToTake;
            }

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

        return transactionToSave;
    }

    private Order getValidOwnedOrder(String orderId) {
        User currentUser = this.getCurrentUser();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (order.getCustomer() == null || !order.getCustomer().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.ORDER_DO_NOT_BELONG);
        }

        return order;
    }

    private void processInventoryRestoration(Order order, TransactionTypeEnum transactionType, boolean isPhysicalReturn) {
        List<InventoryTransaction> trans = inventoryTransactionRepository.findAllByReferenceId(order.getId());
        List<InventoryTransaction> newTransToSave = new ArrayList<>();

        for (InventoryTransaction tran : trans) {
            if (tran.getChangeQty() >= 0) continue;

            InventoryBatch b = tran.getInventoryBatch();

            int refundQty = Math.abs(tran.getChangeQty());

            b.setAvailableQty(b.getAvailableQty() + refundQty);

            // Hoàn lại kho vật lý (chỉ áp dụng khi hàng đã xuất kho và bị trả về)
            if (isPhysicalReturn) {
                b.setPhysicalQty(b.getPhysicalQty() + refundQty);
            }

            InventoryTransaction newTran = InventoryTransaction.builder()
                    .changeQty(refundQty)
                    .inventoryBatch(b)
                    .referenceId(order.getId())
                    .transactionType(transactionType)
                    .build();

            newTransToSave.add(newTran);
        }

        inventoryTransactionRepository.saveAll(newTransToSave);
    }
}
