package com.dtp.cosmemgt.sales.payment.service.impl;

import com.dtp.cosmemgt.core.commonService.MailService;
import com.dtp.cosmemgt.core.exception.AppException;
import com.dtp.cosmemgt.core.exception.ErrorCode;
import com.dtp.cosmemgt.sales.order.entity.Order;
import com.dtp.cosmemgt.sales.order.enums.OrderStatusEnum;
import com.dtp.cosmemgt.sales.order.enums.PaymentStatusEnum;
import com.dtp.cosmemgt.sales.order.repository.OrderRepository;
import com.dtp.cosmemgt.sales.payment.dto.request.PaymentCreationRequest;
import com.dtp.cosmemgt.sales.payment.dto.response.PaymentStatusResponse;
import com.dtp.cosmemgt.sales.payment.dto.response.PaymentResponse;
import com.dtp.cosmemgt.sales.payment.entity.Invoice;
import com.dtp.cosmemgt.sales.payment.paymentConfig.VNPayConfig;
import com.dtp.cosmemgt.sales.payment.repository.InvoiceRepository;
import com.dtp.cosmemgt.sales.payment.service.IPaymentService;
import com.dtp.cosmemgt.sales.payment.service.VNPayUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

@Service("VNPAY")
@RequiredArgsConstructor
@Slf4j
@Transactional
public class VNPayPaymentService implements IPaymentService {

    private final OrderRepository orderRepository;
    private final InvoiceRepository invoiceRepository;
    private final MailService mailService;
    private final VNPayConfig vnPayConfig;

    @Override
    public PaymentResponse createPaymentRequest(PaymentCreationRequest request, String ipAddress) throws Exception {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (order.getInvoice().getPaymentStatus() == PaymentStatusEnum.PAID) {
            throw new AppException(ErrorCode.ORDER_HAS_BEEN_PAID);
        }

        log.info("VNPAY TMN_CODE: [{}]", vnPayConfig.getTmnCode());
        log.info("VNPAY HASH_SECRET length: [{}]", vnPayConfig.getHashSecret() != null ?
                vnPayConfig.getHashSecret().length() : 0);

        long amount = order.getTotalAmount().longValue() * 100;

        String vnp_TxnRef = "VNP" + System.currentTimeMillis();             //transaction reference, unique for each transaction

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnPayConfig.getVersion());
        vnp_Params.put("vnp_Command", vnPayConfig.getCommand());
        vnp_Params.put("vnp_TmnCode", vnPayConfig.getTmnCode());
        vnp_Params.put("vnp_Amount", String.valueOf(amount));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang VNPAY");
        vnp_Params.put("vnp_OrderType", vnPayConfig.getOrderType());
        vnp_Params.put("vnp_ReturnUrl", vnPayConfig.getReturnUrl());
        vnp_Params.put("vnp_Locale", "vn");

        String vnp_IpAddr = (ipAddress == null || ipAddress.isEmpty() || ipAddress.equals("0:0:0:0:0:0:0:1"))
                ? "127.0.0.1" : ipAddress;
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");

        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);
        vnp_Params.put("vnp_ExpireDate", formatter.format(cld.getTime()));

        String queryUrl = VNPayUtils.buildQueryUrl(vnp_Params, vnPayConfig.getHashSecret().trim());
        String paymentUrl = vnPayConfig.getPayUrl() + "?" + queryUrl;

        order.getInvoice().setPaymentStatus(PaymentStatusEnum.PENDING);
        order.getInvoice().setPaymentRequestId(vnp_TxnRef);
        order.getInvoice().setTransactionId(vnp_CreateDate);

        return PaymentResponse.builder()
                .payUrl(paymentUrl)
                .orderId(vnp_TxnRef)
                .build();
    }

    //http://localhost:3000/payment-result?vnp_Amount=59000000&vnp_BankCode=NCB&vnp_BankTranNo=VNP15673226&vnp_CardType=ATM&vnp_OrderInfo=Thanh+toan+don+hang+VNPAY&vnp_PayDate=20260904194304&vnp_ResponseCode=00&vnp_TmnCode=FA3UELQX&vnp_TransactionNo=15673226&vnp_TransactionStatus=00&vnp_TxnRef=VNP1788525762246&vnp_SecureHash=f79dbdce8f0d3bd31930e1165291f155441934fe56bce9964babbad50ae349c9bbb6e3f17c27181b8dc0f3ca740fb831ffd9b486e318e19120361752b28b9690
    public Map<String, String> handleIpn(Map<String, String> params) {
        Map<String, String> response = new HashMap<>();
        try {
            log.info("[VNPAY IPN] Nhận payload: {}", params);

            String vnp_SecureHash = params.get("vnp_SecureHash");
            params.remove("vnp_SecureHash");
            params.remove("vnp_SecureHashType");

            String signValue = VNPayUtils.hashAllFields(params, vnPayConfig.getHashSecret().trim());
            if (!signValue.equals(vnp_SecureHash)) {
                log.error("[VNPAY IPN] Chữ ký không hợp lệ!");
                response.put("RspCode", "97");
                response.put("Message", "Invalid signature");
                return response;
            }

            String vnp_TxnRef = params.get("vnp_TxnRef");
            String vnp_Amount = params.get("vnp_Amount");
            String vnp_ResponseCode = params.get("vnp_ResponseCode");
            String vnp_TransactionNo = params.get("vnp_TransactionNo");

            Invoice invoice = invoiceRepository.findByPaymentRequestId(vnp_TxnRef).orElse(null);
            if (invoice == null) {
                log.error("[VNPAY IPN] Không tìm thấy Hóa đơn mang mã: {}", vnp_TxnRef);
                response.put("RspCode", "01");
                response.put("Message", "Order not found");
                return response;
            }

            Order order = invoice.getOrder();

            long expectedAmount = order.getTotalAmount().longValue() * 100;
            if (expectedAmount != Long.parseLong(vnp_Amount)) {
                log.error("[VNPAY IPN] Sai số tiền. Mong đợi: {}, Thực tế: {}", expectedAmount, vnp_Amount);
                response.put("RspCode", "04");
                response.put("Message", "Invalid amount");
                return response;
            }

            if (invoice.getPaymentStatus() == PaymentStatusEnum.PAID) {
                log.warn("[VNPAY IPN] Hóa đơn {} đã được xác nhận từ trước.", vnp_TxnRef);
                response.put("RspCode", "02");
                response.put("Message", "Order already confirmed");
                return response;
            }

            if ("00".equals(vnp_ResponseCode)) {
                log.info("[VNPAY IPN] Thanh toán THÀNH CÔNG cho Hóa đơn: {}", vnp_TxnRef);
                invoice.setPaymentStatus(PaymentStatusEnum.PAID);
                invoice.setTransactionId(vnp_TransactionNo); // Ghi đè lại bằng TransactionNo thật của VNPAY

                if (order.getOrderStatus() == OrderStatusEnum.PENDING) {
                    order.setOrderStatus(OrderStatusEnum.CONFIRMED);
                }
                mailService.sendOrderConfirmationEmail(order.getCustomer(), order);
            } else {
                log.warn("[VNPAY IPN] Thanh toán THẤT BẠI cho Hóa đơn: {}. Mã lỗi: {}", vnp_TxnRef, vnp_ResponseCode);
                invoice.setPaymentStatus(PaymentStatusEnum.FAILED);
            }

            response.put("RspCode", "00");
            response.put("Message", "Confirm Success");

        } catch (Exception e) {
            log.error("[VNPAY IPN] Lỗi hệ thống khi xử lý IPN", e);
            response.put("RspCode", "99");
            response.put("Message", "Unknown error");
        }
        return response;
    }

    public PaymentStatusResponse checkTransactionStatus(Order order) throws Exception {
        String vnp_TxnRef = order.getInvoice().getPaymentRequestId();
        if (vnp_TxnRef == null) {
            throw new AppException(ErrorCode.INVALID_PAYMENT_REQUEST);
        }

        String vnp_RequestId = UUID.randomUUID().toString();
        String vnp_Version = vnPayConfig.getVersion();
        String vnp_Command = "querydr";
        String vnp_TmnCode = vnPayConfig.getTmnCode();
        String vnp_IpAddr = "127.0.0.1";
        String vnp_OrderInfo = "Truy van don hang " + vnp_TxnRef;

        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        formatter.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));

        String vnp_TransactionDate = order.getInvoice().getTransactionId();

        if (vnp_TransactionDate == null || vnp_TransactionDate.length() != 14) {
            vnp_TransactionDate = formatter.format(Timestamp.valueOf(order.getCreatedAt()));
        }

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        String vnp_CreateDate = formatter.format(cld.getTime());

        String hashData = vnp_RequestId + "|" + vnp_Version + "|" + vnp_Command + "|" +
                vnp_TmnCode + "|" + vnp_TxnRef + "|" + vnp_TransactionDate + "|" +
                vnp_CreateDate + "|" + vnp_IpAddr + "|" + vnp_OrderInfo;

        log.info("[VNPAY QueryDR] HashData gửi đi: {}", hashData);

        String vnp_SecureHash = VNPayUtils.hmacSHA512(vnPayConfig.getHashSecret().trim(), hashData);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("vnp_RequestId", vnp_RequestId);
        requestBody.put("vnp_Version", vnp_Version);
        requestBody.put("vnp_Command", vnp_Command);
        requestBody.put("vnp_TmnCode", vnp_TmnCode);
        requestBody.put("vnp_TxnRef", vnp_TxnRef);
        requestBody.put("vnp_OrderInfo", vnp_OrderInfo);
        requestBody.put("vnp_TransactionDate", vnp_TransactionDate);
        requestBody.put("vnp_CreateDate", vnp_CreateDate);
        requestBody.put("vnp_IpAddr", vnp_IpAddr);
        requestBody.put("vnp_SecureHash", vnp_SecureHash);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        RestTemplate sslRestTemplate = createTrustAllRestTemplate();

        ResponseEntity<Map> response = sslRestTemplate.postForEntity(
                vnPayConfig.getApiUrl(),
                entity,
                Map.class
        );

        Map<String, Object> responseBody = response.getBody();
        if (responseBody != null) {
            String vnp_ResponseCode = (String) responseBody.get("vnp_ResponseCode");
            String vnp_TransactionStatus = (String) responseBody.get("vnp_TransactionStatus");
            String vnp_TransactionNo = (String) responseBody.get("vnp_TransactionNo");

            log.info("[VNPAY QueryDR] Order: {}, ResponseCode: {}, TransStatus: {}", vnp_TxnRef, vnp_ResponseCode, vnp_TransactionStatus);

            if ("00".equals(vnp_ResponseCode) && "00".equals(vnp_TransactionStatus)) {
                return new PaymentStatusResponse(true, vnp_TransactionNo);
            }
        }

        return new PaymentStatusResponse(false, null);
    }

    @Override
    public void rescueMissedPayment(Order order, String transId) {
        if (order.getInvoice().getPaymentStatus() == PaymentStatusEnum.PAID) {
            return;
        }

        log.info("[VNPAY Rescue] Đã giải cứu thành công OrderId: {}. Chuyển sang PAID.", order.getId());

        order.getInvoice().setPaymentStatus(PaymentStatusEnum.PAID);
        order.getInvoice().setTransactionId(transId);

        if (order.getOrderStatus() == OrderStatusEnum.PENDING) {
            order.setOrderStatus(OrderStatusEnum.CONFIRMED);
        }

        orderRepository.save(order);

        mailService.sendOrderConfirmationEmail(order.getCustomer(), order);
    }

    @Override
    public void refund(Order order) throws Exception {
        String vnp_TxnRef = order.getInvoice().getPaymentRequestId();
        String vnp_TransactionNo = order.getInvoice().getTransactionId();

        if (vnp_TxnRef == null || vnp_TransactionNo == null) {
            throw new AppException(ErrorCode.INVALID_PAYMENT_REQUEST);
        }

        String vnp_RequestId = UUID.randomUUID().toString();
        String vnp_Version = vnPayConfig.getVersion();
        String vnp_Command = "refund";
        String vnp_TmnCode = vnPayConfig.getTmnCode();
        String vnp_TransactionType = "02";
        long amount = order.getTotalAmount().longValue() * 100;
        String vnp_Amount = String.valueOf(amount);
        String vnp_CreateBy = "Admin";              //co the thay doi tuong tuong ung voi nguoi thuc hien giao dich
        String vnp_OrderInfo = "Hoan tien don hang " + vnp_TxnRef;
        String vnp_IpAddr = "127.0.0.1";

        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        formatter.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));

        String vnp_TransactionDate = formatter.format(java.sql.Timestamp.valueOf(order.getCreatedAt()));

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        String vnp_CreateDate = formatter.format(cld.getTime());

        String hashData = vnp_RequestId + "|" + vnp_Version + "|" + vnp_Command + "|" +
                vnp_TmnCode + "|" + vnp_TransactionType + "|" + vnp_TxnRef + "|" +
                vnp_Amount + "|" + vnp_TransactionNo + "|" + vnp_TransactionDate + "|" +
                vnp_CreateBy + "|" + vnp_CreateDate + "|" + vnp_IpAddr + "|" + vnp_OrderInfo;

        String vnp_SecureHash = VNPayUtils.hmacSHA512(vnPayConfig.getHashSecret().trim(), hashData);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("vnp_RequestId", vnp_RequestId);
        requestBody.put("vnp_Version", vnp_Version);
        requestBody.put("vnp_Command", vnp_Command);
        requestBody.put("vnp_TmnCode", vnp_TmnCode);
        requestBody.put("vnp_TransactionType", vnp_TransactionType);
        requestBody.put("vnp_TxnRef", vnp_TxnRef);
        requestBody.put("vnp_Amount", vnp_Amount);
        requestBody.put("vnp_TransactionNo", vnp_TransactionNo);
        requestBody.put("vnp_TransactionDate", vnp_TransactionDate);
        requestBody.put("vnp_CreateBy", vnp_CreateBy);
        requestBody.put("vnp_CreateDate", vnp_CreateDate);
        requestBody.put("vnp_IpAddr", vnp_IpAddr);
        requestBody.put("vnp_OrderInfo", vnp_OrderInfo);
        requestBody.put("vnp_SecureHash", vnp_SecureHash);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        // bypass SSL certificate validation for testing purposes
        RestTemplate sslRestTemplate = createTrustAllRestTemplate();
        ResponseEntity<Map> response = sslRestTemplate.postForEntity(vnPayConfig.getApiUrl(), entity, Map.class);

        Map<String, Object> responseBody = response.getBody();
        if (responseBody != null) {
            String vnp_ResponseCode = (String) responseBody.get("vnp_ResponseCode");
            String vnp_Message = (String) responseBody.get("vnp_Message");

            log.info("[VNPAY Refund] Order: {}, ResponseCode: {}, Message: {}", vnp_TxnRef, vnp_ResponseCode, vnp_Message);

            if ("00".equals(vnp_ResponseCode)) {
                order.getInvoice().setPaymentStatus(PaymentStatusEnum.REFUNDED);
                mailService.sendWaitingOrderRefundEmail(order.getCustomer(), order);
            } else {
                log.error("[VNPAY Refund] Lỗi từ VNPAY: {}", vnp_Message);
                throw new RuntimeException("Lỗi hoàn tiền VNPAY: " + vnp_Message);
            }
        }
    }

    private RestTemplate createTrustAllRestTemplate() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                    }
            };

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new SecureRandom());

            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);

            return new RestTemplate(new SimpleClientHttpRequestFactory());
        } catch (Exception e) {
            return new RestTemplate();
        }
    }
}