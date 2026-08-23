package com.dtp.cosmemgt.catalog.controller.admin;

import com.dtp.cosmemgt.catalog.dto.request.UomUpdateRequest;
import com.dtp.cosmemgt.catalog.dto.request.UomCreationRequest;
import com.dtp.cosmemgt.catalog.dto.response.UomResponse;
import com.dtp.cosmemgt.catalog.service.AdminUomService;
import com.dtp.cosmemgt.core.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/uom")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AdminUomController {
    AdminUomService uomService;

    @PostMapping()
    ApiResponse<UomResponse> create(@RequestBody @Valid UomCreationRequest request) {
        return ApiResponse.<UomResponse>builder()
                .result(uomService.create(request))
                .build();
    }

    @GetMapping()
    ApiResponse<List<UomResponse>> getAll() {
        return ApiResponse.<List<UomResponse>>builder()
                .result(uomService.getAll())
                .build();
    }

    @GetMapping("/{uomId}")
    ApiResponse<UomResponse> getUomById(@PathVariable int uomId) {
        return ApiResponse.<UomResponse>builder()
                .result(uomService.getUomById(uomId))
                .build();
    }

    @PutMapping("/{uomId}")
    ApiResponse<UomResponse> updateUom(@PathVariable int uomId,
                                             @RequestBody @Valid UomUpdateRequest request) {
        return ApiResponse.<UomResponse>builder()
                .result(uomService.update(uomId, request))
                .build();
    }

    @DeleteMapping("/soft_del/{uomId}")
    ApiResponse<Void> softDeleteUom(@PathVariable int uomId) {
        uomService.sftDelUom(uomId);
        return ApiResponse.<Void>builder()
                .message("Uom with id " + uomId + " has been soft deleted successfully.")
                .build();
    }

    @PutMapping("/restore/{uomId}")
    ApiResponse<Void> restoreUom(@PathVariable int uomId) {
        uomService.restoreUom(uomId);
        return ApiResponse.<Void>builder()
                .message("Uom with id " + uomId + " has been restored successfully.")
                .build();
    }

    @DeleteMapping("/hard_del/{uomId}")
    ApiResponse<Void> hardDelUom(@PathVariable int uomId) {
        uomService.hardDelUom(uomId);
        return ApiResponse.<Void>builder()
                .message("Uom with id " + uomId + " has been hard deleted successfully.")
                .build();
    }
}