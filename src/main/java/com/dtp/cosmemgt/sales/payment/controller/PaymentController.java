package com.dtp.cosmemgt.sales.payment.controller;

import com.dtp.cosmemgt.core.dto.ApiResponse;
import com.dtp.cosmemgt.core.exception.AppException;
import com.dtp.cosmemgt.core.exception.ErrorCode;
import com.dtp.cosmemgt.sales.payment.dto.request.PaymentCreationRequest;
import com.dtp.cosmemgt.sales.payment.dto.response.PaymentStatusResponse;
import com.dtp.cosmemgt.sales.payment.dto.response.PaymentResponse;
import com.dtp.cosmemgt.sales.payment.service.IPaymentService;
import com.dtp.cosmemgt.sales.payment.service.impl.MomoPaymentService;
import com.dtp.cosmemgt.sales.payment.service.impl.VNPayPaymentService;
import com.dtp.cosmemgt.sales.order.entity.Order;
import com.dtp.cosmemgt.sales.order.repository.OrderRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {
    private final Map<String, IPaymentService> paymentServiceMap;
    private final OrderRepository orderRepository;

    @PostMapping("/pay")
    public ApiResponse<PaymentResponse> createPayment(
            @RequestBody @Valid PaymentCreationRequest request,
            HttpServletRequest httpRequest) throws Exception {

        String ipAddress = httpRequest.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null || ipAddress.isEmpty()) {
            ipAddress = httpRequest.getRemoteAddr();
        }

        String method = request.getPaymentMethod() != null ? request.getPaymentMethod().toUpperCase() : "MOMO";

        IPaymentService paymentService = paymentServiceMap.get(method);
        if (paymentService == null) {
            throw new AppException(ErrorCode.PAYMENT_METHOD_NOT_SUPPORTED);
        }

        return ApiResponse.<PaymentResponse>builder()
                .result(paymentService.createPaymentRequest(request, ipAddress))
                .build();
    }

    @PostMapping("/ipn-url")
    public ResponseEntity<String> handleMomoIpn(@RequestBody String requestBody) {
        MomoPaymentService paymentService = (MomoPaymentService) paymentServiceMap.get("MOMO");
        try {
            paymentService.handleIpn(requestBody);
            return ResponseEntity.noContent().build();

        } catch (Exception e) {
            log.error("Error handling MoMo IPN: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/vnpay-ipn")
    public ResponseEntity<Map<String, String>> handleVnpayIpn(@RequestParam Map<String, String> queryParams) {
        VNPayPaymentService vnPayService = (VNPayPaymentService) paymentServiceMap.get("VNPAY");

        try {
            Map<String, String> response = vnPayService.handleIpn(queryParams);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Lỗi ngoại lệ khi xử lý VNPay IPN: {}", e.getMessage(), e);

            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("RspCode", "99");
            errorResponse.put("Message", "Unknown error");
            return ResponseEntity.ok(errorResponse);
        }
    }

    @GetMapping("/check-status/{orderId}")
    public ApiResponse<PaymentStatusResponse> checkPaymentStatus(@PathVariable String orderId) throws Exception {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        String paymentMethod = order.getInvoice().getPaymentMethod().name();
        IPaymentService paymentService = paymentServiceMap.get(paymentMethod);

        if (paymentService == null) {
            throw new AppException(ErrorCode.STATUS_CHECK_NOT_SUPPORTED);
        }

        PaymentStatusResponse status = paymentService.checkTransactionStatus(order);

        if (status.isPaid()) {
            paymentService.rescueMissedPayment(order, status.getTransId());
        }

        return ApiResponse.<PaymentStatusResponse>builder()
                .result(status)
                .build();
    }
}