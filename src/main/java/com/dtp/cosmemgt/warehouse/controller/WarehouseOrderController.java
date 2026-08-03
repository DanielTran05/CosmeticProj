package com.dtp.cosmemgt.warehouse.controller;

import com.dtp.cosmemgt.core.dto.ApiResponse;
import com.dtp.cosmemgt.core.dto.PageResponse;
import com.dtp.cosmemgt.sales.customer.dto.response.OrderResponse;
import com.dtp.cosmemgt.sales.internal.dto.response.WarehouseOrderResponse;
import com.dtp.cosmemgt.warehouse.service.WarehouseOrderService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/warehouse/outbound/orders")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WarehouseOrderController {

    WarehouseOrderService warehouseOrderService;

    //ds don hang cho xuat kho                                                  CONFIRMED
    @GetMapping
    public ApiResponse<PageResponse<OrderResponse>> getAllOrderToExport(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        return ApiResponse.<PageResponse<OrderResponse>>builder()
                .result(warehouseOrderService.getAllOrderToExport(page, size))
                .build();
    }

    // chi tiet don hang de xuat hang
    @GetMapping("/{orderId}")
    public ApiResponse<WarehouseOrderResponse> getOrderDetailToExport(@PathVariable String orderId) {
        
        return ApiResponse.<WarehouseOrderResponse>builder()
                .result(warehouseOrderService.getOrderDetailToExport(orderId))
                .build();
    }

    //xac nhan xuat giao cho dvvc                                                   CONFIRMED → SHIPPING
    @PutMapping("/{orderId}/export")
    public ApiResponse<Void> orderExportForShipping(@PathVariable String orderId) {
        
        warehouseOrderService.orderExportForShipping(orderId);
        
        return ApiResponse.<Void>builder()
                .message("Xuất kho thành công")
                .build();
    }

    //xac nhan hoan hang                                                            COMPLETED||RETURN_REQUEST → RETURNED
    @PutMapping("/{orderId}/confirmReturn")
    public ApiResponse<Void> confirmReturnOrder(@PathVariable String orderId) throws Exception {
        
        warehouseOrderService.warehousConfirmReturnOrder(orderId);
        
        return ApiResponse.<Void>builder()
                .message("Xác nhận nhận hàng hoàn thành công")
                .build();
    }

    //huy don tu kho                                                                  → CANCELED
    @PutMapping("/{orderId}/cancel")
    public ApiResponse<Void> cancelOrderFromWarehouse(@PathVariable String orderId) throws Exception {
        
        warehouseOrderService.cancelOrderFromWarehouse(orderId);
        
        return ApiResponse.<Void>builder()
                .message("Hủy đơn hàng thành công")
                .build();
    }

    //xac nhan giao thanh cong DVVC                                                     SHIPPING → COMPLETED
    @PutMapping("/{orderId}/deliver")
    public ApiResponse<Void> confirmDelivered(@PathVariable String orderId) {
        
        warehouseOrderService.confirmDelivered(orderId);
        
        return ApiResponse.<Void>builder()
                .message("Xác nhận giao hàng thành công")
                .build();
    }
}