package com.dtp.cosmemgt.sales.payment.service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class VNPayUtils {

    public static String buildQueryUrl(Map<String, String> fields, String secretKey) {
        // 1. Sắp xếp key theo alphabet
        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        // 2. Duyệt qua từng param và chỉ nối khi có giá trị
        boolean isFirst = true;
        for (String fieldName : fieldNames) {
            String fieldValue = fields.get(fieldName);
            if (fieldValue != null && !fieldValue.trim().isEmpty()) {
                try {
                    String encodedValue = URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.toString());

                    if (!isFirst) {
                        hashData.append('&');
                        query.append('&');
                    }

                    // Tên param giữ nguyên, giá trị được encode UTF-8
                    hashData.append(fieldName).append('=').append(encodedValue);
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.UTF_8.toString()))
                            .append('=').append(encodedValue);

                    isFirst = false;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        // 3. Băm HMAC-SHA512
        String vnp_SecureHash = hmacSHA512(secretKey, hashData.toString());
        return query.toString() + "&vnp_SecureHash=" + vnp_SecureHash;
    }

    public static String hashAllFields(Map<String, String> fields, String secretKey) {
        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        boolean isFirst = true;
        for (String fieldName : fieldNames) {
            String fieldValue = fields.get(fieldName);
            if (fieldValue != null && !fieldValue.trim().isEmpty()) {
                try {
                    String encodedValue = URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.toString());
                    if (!isFirst) {
                        hashData.append('&');
                    }
                    hashData.append(fieldName).append('=').append(encodedValue);
                    isFirst = false;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return hmacSHA512(secretKey, hashData.toString());
    }

    public static String hmacSHA512(final String key, final String data) {
        try {
            if (key == null || data == null) {
                throw new NullPointerException();
            }
            final Mac hmac512 = Mac.getInstance("HmacSHA512");
            byte[] hmacKeyBytes = key.trim().getBytes(StandardCharsets.UTF_8);
            final SecretKeySpec secretKey = new SecretKeySpec(hmacKeyBytes, "HmacSHA512");
            hmac512.init(secretKey);
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            byte[] result = hmac512.doFinal(dataBytes);
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception ex) {
            return "";
        }
    }
}