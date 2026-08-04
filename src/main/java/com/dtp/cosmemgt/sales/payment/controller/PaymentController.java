package com.dtp.cosmemgt.sales.payment.controller;

import com.dtp.cosmemgt.core.dto.ApiResponse;
import com.dtp.cosmemgt.sales.payment.dto.request.PaymentCreationRequest;
import com.dtp.cosmemgt.sales.payment.dto.response.PaymentResponse;
import com.dtp.cosmemgt.sales.payment.service.PaymentService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PaymentController {
    PaymentService paymentService;

    @PostMapping("/pay")
    public ApiResponse<PaymentResponse> pay (@RequestBody PaymentCreationRequest request) throws Exception {
        return ApiResponse.<PaymentResponse>builder()
                .result(paymentService.createPaymentRequest(request))
                .build();
    }

    @PostMapping("/ipn-url")
    public ResponseEntity<String> handleMomoIpn(@RequestBody String requestBody) {
        try {
            paymentService.handleIpn(requestBody);
            return ResponseEntity.noContent().build();

        } catch (Exception e) {
            log.error("Error handling MoMo IPN: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
