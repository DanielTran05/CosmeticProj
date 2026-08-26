package com.dtp.cosmemgt.sales.order.controller;

import com.dtp.cosmemgt.core.dto.ApiResponse;
import com.dtp.cosmemgt.core.dto.PageResponse;
import com.dtp.cosmemgt.sales.order.dto.response.OrderResponse;
import com.dtp.cosmemgt.sales.order.service.command.RefundOrderService;
import com.dtp.cosmemgt.sales.order.service.command.ReturnOrderService;
import com.dtp.cosmemgt.sales.order.service.query.OrderQueryService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AdminOrderController {
    RefundOrderService refundOrderService;
    OrderQueryService orderQueryService;

    @GetMapping
    public ApiResponse<PageResponse<OrderResponse>> getAllOrders(@RequestParam Map<String, String> queryParams) {
        return ApiResponse.<PageResponse<OrderResponse>>builder()
                .result(orderQueryService.getAllOrderAdmin(queryParams))
                .build();
    }

    @GetMapping("/pending-manual-refunds")
    public ApiResponse<List<OrderResponse>> getPendingManualRefunds() {

        return ApiResponse.<List<OrderResponse>>builder()
                .result(orderQueryService.getOrderAdminRefund())
                .build();
    }

    @PostMapping("/{orderId}/refund")
    public void adminManualConfirmRefund(@PathVariable String orderId) throws Exception {
        refundOrderService.adminManualConfirmRefund(orderId);
    }
}
