package com.dtp.cosmemgt.warehouse.controller;

import com.dtp.cosmemgt.core.dto.ApiResponse;
import com.dtp.cosmemgt.core.dto.PageResponse;
import com.dtp.cosmemgt.sales.order.dto.response.OrderResponse;
import com.dtp.cosmemgt.sales.order.enums.OrderStatusEnum;
import com.dtp.cosmemgt.sales.order.service.command.ReturnOrderService;
import com.dtp.cosmemgt.warehouse.dto.response.WarehouseOrderResponse;
import com.dtp.cosmemgt.warehouse.service.WarehouseOrderService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/warehouse/outbound/orders")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WarehouseOrderController {

    WarehouseOrderService warehouseOrderService;
    ReturnOrderService returnOrderService;

    @GetMapping
    public ApiResponse<PageResponse<OrderResponse>> getAllOrders(
            @RequestParam(required = false) OrderStatusEnum status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ApiResponse.<PageResponse<OrderResponse>>builder()
                .result(warehouseOrderService.getAllOrder(status, page, size))
                .build();
    }

    @GetMapping("/{orderId}")
    public ApiResponse<WarehouseOrderResponse> getOrderDetail(@PathVariable String orderId) {
        
        return ApiResponse.<WarehouseOrderResponse>builder()
                .result(warehouseOrderService.getOrderDetailToExport(orderId))
                .build();
    }

    @PutMapping("{orderId}/packing")
    public ApiResponse<Void> packOrder(@PathVariable String orderId) {

        warehouseOrderService.packOrder(orderId);

        return ApiResponse.<Void>builder()
                .message("Nhan don thanh cong")
                .build();
    }

    @PutMapping("/{orderId}/export")
    public ApiResponse<Void> orderExportForShipping(@PathVariable String orderId) {
        
        warehouseOrderService.orderExportForShipping(orderId);
        
        return ApiResponse.<Void>builder()
                .message("Xuất kho thành công")
                .build();
    }

    @PutMapping("/{orderId}/confirmReturn")
    public ApiResponse<Void> confirmReturnOrder(@PathVariable String orderId) throws Exception {
        
        warehouseOrderService.warehousConfirmReturnOrder(orderId);
        
        return ApiResponse.<Void>builder()
                .message("Xác nhận nhận hàng hoàn thành công")
                .build();
    }

    @PutMapping("/{orderId}/confirmDeliveryFailed")
    public ApiResponse<Void> warehouseConfirmFailedOrder(@PathVariable String orderId) throws Exception {

        warehouseOrderService.warehouseConfirmFailedOrder(orderId);

        return ApiResponse.<Void>builder()
                .message("Xác nhận nhận hàng hoàn thành công")
                .build();
    }

    @PutMapping("/{orderId}/cancel")
    public ApiResponse<Void> cancelOrderfromWarehouse(@PathVariable String orderId) throws Exception {
        
        warehouseOrderService.cancelOrderfromWarehouse(orderId);
        
        return ApiResponse.<Void>builder()
                .message("Hủy đơn hàng thành công")
                .build();
    }

    @PutMapping("/{orderId}/deliver")
    public ApiResponse<Void> confirmDelivered(@PathVariable String orderId) {
        
        warehouseOrderService.markOrderCompleted(orderId);
        
        return ApiResponse.<Void>builder()
                .message("Xác nhận giao hàng thành công")
                .build();
    }

    @PostMapping("/{orderId}/mock-failed")
    public ApiResponse<Void> mockShipperDeliveryFailed(@PathVariable String orderId) throws Exception {
        returnOrderService.mockShipperDeliveryFailed(orderId);
        return ApiResponse.<Void>builder()
                .message("Order has been failed, delivery failed, wait for returning")
                .build();
    }
}