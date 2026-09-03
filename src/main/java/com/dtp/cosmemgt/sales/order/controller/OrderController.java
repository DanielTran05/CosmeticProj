package com.dtp.cosmemgt.sales.order.controller;

import com.dtp.cosmemgt.core.dto.ApiResponse;
import com.dtp.cosmemgt.core.dto.PageResponse;
import com.dtp.cosmemgt.sales.order.dto.request.CartCalculateRequest;
import com.dtp.cosmemgt.sales.order.dto.request.OrderCreationRequest;
import com.dtp.cosmemgt.sales.order.dto.response.CartCalculateResponse;
import com.dtp.cosmemgt.sales.order.dto.response.OrderDetailResponse;
import com.dtp.cosmemgt.sales.order.dto.response.OrderResponse;
import com.dtp.cosmemgt.sales.order.service.command.CancelOrderService;
import com.dtp.cosmemgt.sales.order.service.command.CartCalculationService;
import com.dtp.cosmemgt.sales.order.service.command.PlaceOrderService;
import com.dtp.cosmemgt.sales.order.service.command.ReturnOrderService;
import com.dtp.cosmemgt.sales.order.service.query.OrderQueryService;
import jakarta.validation.Valid;
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
    OrderQueryService customerOrderQueryService;
    CartCalculationService cartCalculationService;
    CancelOrderService cancelOrderService;

    //COMMAND

    @PostMapping
    public ApiResponse<OrderResponse> createOrder(@RequestBody @Valid OrderCreationRequest request) {
        return ApiResponse.<OrderResponse>builder()
                .result(placeOrderService.create(request))
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

    @PostMapping("/{orderId}/return/cancel")
    public ApiResponse<Void> cancelReturnRequest(@PathVariable String orderId) throws Exception {
        returnOrderService.cancelReturnRequest(orderId);
        return ApiResponse.<Void>builder()
                .message("Order returned successfully")
                .build();
    }


    //QUERY

    @GetMapping
    public ApiResponse<PageResponse<OrderResponse>> getAllMyOrders(@RequestParam Map<String, String> queryParams) {
        return ApiResponse.<PageResponse<OrderResponse>>builder()
                .result(customerOrderQueryService.getAllMyOrder(queryParams))
                .build();
    }

    @GetMapping("/{orderId}")
    public ApiResponse<OrderDetailResponse> getOrderDetail(@PathVariable String orderId) {
        return ApiResponse.<OrderDetailResponse>builder()
                .result(customerOrderQueryService.getOrderDetail(orderId))
                .build();
    }

    @PostMapping("/calculate")
    public ApiResponse<CartCalculateResponse> calculateOrder(@RequestBody CartCalculateRequest request) {
        return ApiResponse.<CartCalculateResponse>builder()
                .result(cartCalculationService.calculate(request))
                .build();
    }
}
