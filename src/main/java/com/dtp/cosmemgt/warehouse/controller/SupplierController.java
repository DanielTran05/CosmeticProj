package com.dtp.cosmemgt.warehouse.controller;
import com.dtp.cosmemgt.core.dto.ApiResponse;
import com.dtp.cosmemgt.warehouse.dto.request.SupplierCreationRequest;
import com.dtp.cosmemgt.warehouse.dto.response.SupplierResponse;
import com.dtp.cosmemgt.warehouse.service.SupplierService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/suppliers")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class SupplierController {
    SupplierService supplierService;

    @PostMapping()
    ApiResponse<SupplierResponse> create(@RequestBody SupplierCreationRequest request) {
        return ApiResponse.<SupplierResponse>builder()
                .result(supplierService.create(request))
                .build();
    }

    @GetMapping()
    ApiResponse<List<SupplierResponse>> getAll() {
        return ApiResponse.<List<SupplierResponse>>builder()
                .result(supplierService.getAll())
                .build();
    }

    @GetMapping("/{supplierId}")
    ApiResponse<SupplierResponse> getSplById(@PathVariable int supplierId) {
        return ApiResponse.<SupplierResponse>builder()
                .result(supplierService.getSplById(supplierId))
                .build();
    }

    @PutMapping("/{supplierId}")
    ApiResponse<SupplierResponse> updateSpl(@PathVariable int supplierId,
                                             @RequestBody SupplierCreationRequest request) {
        return ApiResponse.<SupplierResponse>builder()
                .result(supplierService.update(supplierId, request))
                .build();
    }

    @DeleteMapping("/soft_del/{supplierId}")
    ApiResponse<Void> softDeleteSpl(@PathVariable int supplierId) {
        supplierService.sftDelSpl(supplierId);
        return ApiResponse.<Void>builder()
                .message("Supplier with id " + supplierId + " has been soft deleted successfully.")
                .build();
    }

    @PutMapping("/restore/{supplierId}")
    ApiResponse<Void> restoreSpl(@PathVariable int supplierId) {
        supplierService.restoreSpl(supplierId);
        return ApiResponse.<Void>builder()
                .message("Supplier with id " + supplierId + " has been restored successfully.")
                .build();
    }

    @DeleteMapping("/hard_del/{supplierId}")
    ApiResponse<Void> hardDelSpl(@PathVariable int supplierId) {
        supplierService.hardDelSpl(supplierId);
        return ApiResponse.<Void>builder()
                .message("Supplier with id " + supplierId + " has been hard deleted successfully.")
                .build();
    }
}