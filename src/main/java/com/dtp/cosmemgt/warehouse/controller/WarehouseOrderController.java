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
@RequestMapping("/admin/warehouse/outbound/orders")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WarehouseOrderController {

    WarehouseOrderService warehouseOrderService;

    // 1. Lấy danh sách các đơn hàng chờ xuất kho
    @GetMapping
    public ApiResponse<PageResponse<OrderResponse>> getAllOrderToExport(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        return ApiResponse.<PageResponse<OrderResponse>>builder()
                .result(warehouseOrderService.getAllOrderToExport(page, size))
                .build();
    }

    // 2. Lấy chi tiết đơn hàng để thủ kho nhặt hàng (Pick & Pack)
    @GetMapping("/{orderId}")
    public ApiResponse<WarehouseOrderResponse> getOrderDetailToExport(@PathVariable String orderId) {
        
        return ApiResponse.<WarehouseOrderResponse>builder()
                .result(warehouseOrderService.getOrderDetailToExport(orderId))
                .build();
    }

    // 3. Xác nhận xuất kho giao cho Đơn vị vận chuyển
    @PutMapping("/{orderId}/export")
    public ApiResponse<Void> orderExportForShipping(@PathVariable String orderId) {
        
        warehouseOrderService.orderExportForShipping(orderId);
        
        return ApiResponse.<Void>builder()
                .message("Xuất kho thành công")
                .build();
    }

    // 4. Xác nhận nhận lại hàng hoàn (Do khách bom hàng/trả hàng)
    @PutMapping("/{orderId}/return")
    public ApiResponse<Void> confirmReturnOrder(@PathVariable String orderId) {
        
        warehouseOrderService.confirmReturnOrder(orderId);
        
        return ApiResponse.<Void>builder()
                .message("Xác nhận nhận hàng hoàn thành công")
                .build();
    }

    // 5. Hủy đơn từ phía kho (Do kiểm tra thấy hàng lỗi, rách bao bì không thể giao)
    @PutMapping("/{orderId}/cancel")
    public ApiResponse<Void> cancelOrderFromWarehouse(@PathVariable String orderId) {
        
        warehouseOrderService.cancelOrderFromWarehouse(orderId);
        
        return ApiResponse.<Void>builder()
                .message("Hủy đơn hàng thành công")
                .build();
    }

    // 6. Xác nhận đơn hàng đã giao thành công (Thường do Webhook ĐVVC gọi về)
    @PutMapping("/{orderId}/deliver")
    public ApiResponse<Void> confirmDelivered(@PathVariable String orderId) {
        
        warehouseOrderService.confirmDelivered(orderId);
        
        return ApiResponse.<Void>builder()
                .message("Xác nhận giao hàng thành công")
                .build();
    }
}