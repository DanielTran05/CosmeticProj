package com.dtp.cosmemgt.sales.customer.service;

import com.dtp.cosmemgt.admin.entity.User;
import com.dtp.cosmemgt.core.exception.AppException;
import com.dtp.cosmemgt.core.exception.ErrorCode;
import com.dtp.cosmemgt.core.commonService.MailService;
import com.dtp.cosmemgt.sales.customer.dto.request.PaymentCreationRequest;
import com.dtp.cosmemgt.sales.customer.dto.response.PaymentResponse;
import com.dtp.cosmemgt.sales.entity.Order;
import com.dtp.cosmemgt.sales.enums.OrderStatusEnum;
import com.dtp.cosmemgt.sales.enums.PaymentStatusEnum;
import com.dtp.cosmemgt.sales.repository.InvoiceRepository;
import com.dtp.cosmemgt.sales.repository.OrderRepository;
import com.dtp.cosmemgt.warehouse.service.WarehouseOrderService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Transactional
@Slf4j
public class PaymentService {
    private final InvoiceRepository invoiceRepository;
    @Value("${momo.partner-code}")
    private String PARTNER_CODE;
    @Value("${momo.access-key}")
    private String ACCESS_KEY;
    @Value("${momo.secret-key}")
    private String SECRET_KEY;
    @Value("${momo.redirect-url}")
    private String REDIRECT_URL;
    @Value("${momo.ipn-url}")
    private String IPN_URL;
    @Value("${momo.api-endpoint}")
    private String API_ENDPOINT;
    private String REQUEST_TYPE = "captureWallet";

    final OrderRepository orderRepository;
    final RestTemplate restTemplate;
    final WarehouseOrderService warehouseOrderService;
    final MailService mailService;
    final  ObjectMapper mapper;

    public PaymentResponse createPaymentRequest(PaymentCreationRequest request) throws Exception {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (order.getInvoice().getPaymentStatus() == PaymentStatusEnum.PAID) {
            throw new AppException(ErrorCode.ORDER_HAS_BEEN_PAID);
        }

        long amountToPay = order.getTotalAmount().longValue();
        String requestId = PARTNER_CODE + System.currentTimeMillis();

        String momoOrderId = "ORDER_" + order.getId() + "_" + System.currentTimeMillis();
        String orderInfo = "Thanh toan don hang " + order.getId();
        String extraData = "";

        String rawSignature = String.format(
                "accessKey=%s&amount=%d&extraData=%s&ipnUrl=%s&orderId=%s&orderInfo=%s&partnerCode=%s&redirectUrl=%s&requestId=%s&requestType=%s",
                ACCESS_KEY, amountToPay, extraData, IPN_URL, momoOrderId, orderInfo, PARTNER_CODE, REDIRECT_URL, requestId, REQUEST_TYPE);

        String signature = signHmacSHA256(rawSignature, SECRET_KEY);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("partnerCode", PARTNER_CODE);
        requestBody.put("requestId", requestId);
        requestBody.put("amount", amountToPay);
        requestBody.put("orderId", momoOrderId);
        requestBody.put("orderInfo", orderInfo);
        requestBody.put("redirectUrl", REDIRECT_URL);
        requestBody.put("ipnUrl", IPN_URL);
        requestBody.put("extraData", extraData);
        requestBody.put("requestType", REQUEST_TYPE);
        requestBody.put("signature", signature);
        requestBody.put("lang", "en");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                API_ENDPOINT + "/create",
                requestEntity,
                Map.class
        );

        Map<String, Object> responseBody = response.getBody();

        if (responseBody == null || !Integer.valueOf(0).equals(responseBody.get("resultCode"))) {
            String errorMsg = responseBody != null ? (String) responseBody.get("message") : "Unknown Error";
            log.error("MoMo payment creation failed: {}", errorMsg);
            throw new AppException(ErrorCode.MOMO_PAYMENT_FAILED);
        }

        order.getInvoice().setPaymentStatus(PaymentStatusEnum.PENDING);

        return PaymentResponse.builder()
                .payUrl(responseBody.get("payUrl").toString())
                .orderId(momoOrderId)
                .build();
    }

    public void refund(Order order) throws Exception {
        String transId = order.getInvoice().getTransactionId();
        Long amount = order.getTotalAmount().longValue();

        String requestId = UUID.randomUUID().toString();
        String refundOrderId = "REFUND_" + System.currentTimeMillis();
        String description = "Hoan tien don hang";

        String rawSignature = String.format(
                "accessKey=%s&amount=%d&description=%s&orderId=%s&partnerCode=%s&requestId=%s&transId=%s",
                ACCESS_KEY, amount, description, refundOrderId, PARTNER_CODE, requestId, transId
        );

        try{
            String signature = signHmacSHA256(rawSignature, SECRET_KEY);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("partnerCode", PARTNER_CODE);
            requestBody.put("orderId", refundOrderId);
            requestBody.put("requestId", requestId);
            requestBody.put("amount", amount);
            requestBody.put("transId", transId);
            requestBody.put("description", description);
            requestBody.put("signature", signature);
            requestBody.put("lang", "en");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(API_ENDPOINT + "/refund",
                    entity, Map.class);

            Map<String, Object> responseBody = response.getBody();

            if(response != null && Integer.valueOf(0).equals(responseBody.get("resultCode"))){
                log.info("[MOMO Refund] Refund successfully for Order {}", order.getId());

                order.getInvoice().setPaymentStatus(PaymentStatusEnum.REFUNDED);
            }else{
                String error = responseBody != null ? (String) responseBody.get("message") : "Unknown error";
                log.error("[MOMO Refund] Refund failed for OrderId {}", order.getId());
                throw new AppException(ErrorCode.MOMO_REFUND_FAILED);
            }
        }catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("[MoMo Refund] Error executing refund request", e);
            throw new RuntimeException("MoMo refund execution failed", e);
        }
    }


    public void handleIpn(String requestBody) {
        try {
            JsonNode ipnData = mapper.readTree(requestBody);
            log.info("[MoMo IPN] Received payload: {}", ipnData.toString());

            String partnerCode = ipnData.path("partnerCode").asText();
            String momoOrderId = ipnData.path("orderId").asText();
            String requestId = ipnData.path("requestId").asText();
            long amount = ipnData.path("amount").asLong();
            String orderInfo = ipnData.path("orderInfo").asText();
            String orderType = ipnData.path("orderType").asText();
            long transId = ipnData.path("transId").asLong();
            int resultCode = ipnData.path("resultCode").asInt();
            String message = ipnData.path("message").asText();
            String payType = ipnData.path("payType").asText();
            long responseTime = ipnData.path("responseTime").asLong();
            String extraData = ipnData.has("extraData") ? ipnData.get("extraData").asText() : "";
            String signatureFromMomo = ipnData.path("signature").asText();

            String rawSignature = String.format(
                    "accessKey=%s&amount=%d&extraData=%s&message=%s&orderId=%s&orderInfo=%s&orderType=%s&partnerCode=%s&payType=%s&requestId=%s&responseTime=%d&resultCode=%d&transId=%d",
                    ACCESS_KEY, amount, extraData, message, momoOrderId, orderInfo, orderType, partnerCode, payType, requestId, responseTime, resultCode, transId
            );

            String mySignature = signHmacSHA256(rawSignature, SECRET_KEY);

            if (!mySignature.equals(signatureFromMomo)) {
                log.error("[MoMo IPN] Security Alert! Signature mismatch for MoMo OrderId: {}", momoOrderId);
                return;
            }

            String[] parts = momoOrderId.split("_");
            if (parts.length < 2) {
                log.error("[MoMo IPN] Invalid orderId format from MoMo: {}", momoOrderId);
                return;
            }
            String realOrderId = parts[1];

            Order order = orderRepository.findById(realOrderId)
                    .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

            if (order.getInvoice().getPaymentStatus() == PaymentStatusEnum.PAID) {
                log.warn("[MoMo IPN] Order {} is already PAID. IPN ignored.", realOrderId);
                return;
            }

            if (resultCode == 0) {
                log.info("[MoMo IPN] Payment SUCCESS for OrderId: {}", realOrderId);
                order.getInvoice().setPaymentStatus(PaymentStatusEnum.PAID);
                order.getInvoice().setTransactionId(String.valueOf(transId));

                if (order.getOrderStatus() == OrderStatusEnum.PENDING) {
                    order.setOrderStatus(OrderStatusEnum.CONFIRMED);
                }

                this.sendOrderConfirmationEmail(order.getCustomer(), order);
            } else {
                log.warn("[MoMo IPN] Payment FAILED for OrderId: {}. Message: {}", realOrderId, message);
                order.getInvoice().setPaymentStatus(PaymentStatusEnum.FAILED);

                warehouseOrderService.cancelOrderDueToPaymentFailure(realOrderId);
            }

        } catch (Exception e) {
            log.error("[MoMo IPN] Error processing IPN payload", e);
        }
    }

    private static String signHmacSHA256(String data, String key) throws Exception {
        Mac hmacSHA256 = Mac.getInstance("HmacSHA256");
        //key
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        hmacSHA256.init(secretKey);

        //data
        byte[] hash = hmacSHA256.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1)
                hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private void sendOrderConfirmationEmail(User user, Order order) {
        String subject = "Cảm ơn bạn đã đặt hàng tại CosmeMgt - Mã đơn #" + order.getId();
        String htmlBody = String.format("""
            <div style="font-family: Arial, sans-serif; line-height: 1.6;">
                <h2>Xin chào %s,</h2>
                <p>Cảm ơn bạn đã đặt hàng tại <b>CosmeMgt</b>. Đơn hàng của bạn đã được tiếp nhận thành công!</p>
                <ul>
                    <li><b>Mã đơn hàng:</b> %s</li>
                    <li><b>Tổng tiền:</b> %,d VNĐ</li>
                    <li><b>Trạng thái:</b> Chờ xác nhận</li>
                </ul>
                <p>Chúng tôi sẽ sớm giao hàng đến cho bạn!</p>
            </div>
            """, user.getFullName(), order.getId(), order.getTotalAmount().longValue());

        mailService.sendEmail(user.getEmail(), user.getFullName(), subject, htmlBody);
    }
}