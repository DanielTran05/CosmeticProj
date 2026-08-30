package com.dtp.cosmemgt.sales.promotion.enums;

public enum ScopeType {
    ORDER,   // Giảm toàn đơn
    PRODUCT, // Giảm cho tất cả biến thể của Product này
    VARIANT  // Chỉ giảm cho 1 mã SKU/Biến thể cụ thể
}