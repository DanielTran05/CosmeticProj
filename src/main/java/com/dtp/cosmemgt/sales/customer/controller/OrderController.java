package com.dtp.cosmemgt.sales.customer.controller;

import com.dtp.cosmemgt.core.dto.ApiResponse;
import com.dtp.cosmemgt.core.dto.PageResponse;
import com.dtp.cosmemgt.sales.customer.dto.request.OrderCreationRequest;
import com.dtp.cosmemgt.sales.customer.dto.response.OrderDetailResponse;
import com.dtp.cosmemgt.sales.customer.dto.response.OrderResponse;
import com.dtp.cosmemgt.sales.customer.service.OrderService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class OrderController {
    OrderService orderService;

    @PostMapping
    public ApiResponse<OrderResponse> createOrder(@RequestBody OrderCreationRequest request) {
        return ApiResponse.<OrderResponse>builder()
                .result(orderService.create(request))
                .build();
    }

    @GetMapping
    public ApiResponse<PageResponse<OrderResponse>> getAllMyOrders(@RequestParam Map<String, String> queryParams) {
        return ApiResponse.<PageResponse<OrderResponse>>builder()
                .result(orderService.getAllMyOrder(queryParams))
                .build();
    }

    // 3. Xem chi tiết 1 đơn hàng
    @GetMapping("/{orderId}")
    public ApiResponse<OrderDetailResponse> getOrderDetail(@PathVariable String orderId) {
        return ApiResponse.<OrderDetailResponse>builder()
                .result(orderService.getOrderDetail(orderId))
                .build();
    }

    @PutMapping("/{orderId}/cancel")
    public ApiResponse<Void> cancelOrder(@PathVariable String orderId) throws Exception {
        orderService.cancelOrder(orderId);
        return ApiResponse.<Void>builder()
                .message("Order cancelled successfully")
                .build();
    }

    @PutMapping("/{orderId}/return")
    public ApiResponse<Void> returnOrder(@PathVariable String orderId) {
        orderService.returnOrder(orderId);
        return ApiResponse.<Void>builder()
                .message("Order returned successfully")
                .build();
    }




}
