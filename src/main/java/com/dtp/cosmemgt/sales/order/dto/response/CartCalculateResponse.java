package com.dtp.cosmemgt.sales.order.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CartCalculateResponse {
    BigDecimal subTotal;        // Tổng tiền hàng (đã tính theo giá giảm của từng Variant)
    BigDecimal voucherDiscount; // Số tiền được giảm từ Voucher Order
    BigDecimal finalTotal;      // Tổng tiền cuối cùng khách phải trả
    String appliedVoucherCode;  // Mã voucher áp dụng thành công
    String errorMsg;            // Lỗi nếu voucher không hợp lệ (để FE báo đỏ)
}