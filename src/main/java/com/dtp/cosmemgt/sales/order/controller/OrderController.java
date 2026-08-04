package com.dtp.cosmemgt.sales.order.controller;

import com.dtp.cosmemgt.core.dto.ApiResponse;
import com.dtp.cosmemgt.core.dto.PageResponse;
import com.dtp.cosmemgt.sales.order.dto.request.OrderCreationRequest;
import com.dtp.cosmemgt.sales.order.dto.response.OrderDetailResponse;
import com.dtp.cosmemgt.sales.order.dto.response.OrderResponse;
import com.dtp.cosmemgt.sales.order.service.command.CancelOrderService;
import com.dtp.cosmemgt.sales.order.service.command.PlaceOrderService;
import com.dtp.cosmemgt.sales.order.service.command.ReturnOrderService;
import com.dtp.cosmemgt.sales.order.service.query.CustomerOrderQueryService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class OrderController {
    PlaceOrderService placeOrderService;
    ReturnOrderService returnOrderService;
    CustomerOrderQueryService customerOrderQueryService;
    CancelOrderService cancelOrderService;

    @PostMapping
    public ApiResponse<OrderResponse> createOrder(@RequestBody OrderCreationRequest request) {
        return ApiResponse.<OrderResponse>builder()
                .result(placeOrderService.create(request))
                .build();
    }

    @GetMapping
    public ApiResponse<PageResponse<OrderResponse>> getAllMyOrders(@RequestParam Map<String, String> queryParams) {
        return ApiResponse.<PageResponse<OrderResponse>>builder()
                .result(customerOrderQueryService.getAllMyOrder(queryParams))
                .build();
    }

    // Xem chi tiet don hang
    @GetMapping("/{orderId}")
    public ApiResponse<OrderDetailResponse> getOrderDetail(@PathVariable String orderId) {
        return ApiResponse.<OrderDetailResponse>builder()
                .result(customerOrderQueryService.getOrderDetail(orderId))
                .build();
    }

    @PutMapping("/{orderId}/cancel")
    public ApiResponse<Void> cancelOrder(@PathVariable String orderId) throws Exception {
        cancelOrderService.cancelOrder(orderId);
        return ApiResponse.<Void>builder()
                .message("Order cancelled successfully")
                .build();
    }

    @PutMapping("/{orderId}/return")
    public ApiResponse<Void> returnOrder(@PathVariable String orderId) throws Exception {
        returnOrderService.returnOrder(orderId);
        return ApiResponse.<Void>builder()
                .message("Order returned successfully")
                .build();
    }
}
