package com.dtp.cosmemgt.sales.order.enums;

public enum OrderStatusEnum {
    PENDING,
    CONFIRMED,
    PROCESSING,
    SHIPPING,
    COMPLETED,
    CANCELLED,
    RETURN_REQUESTED,       //yeu cau tra hang cho hang ve kho
    RETURNED,
    DELIVERY_FAILED,

    //PENDING: Khách vừa đặt xong, hàng vẫn nằm trên kệ (đã xí chỗ available_qty).
    //CONFIRMED (Đã xác nhận) thuộc về Luồng hàng hóa. Nó đại diện cho hành động: Hệ thống hoặc Admin đã ghi nhận đơn hàng hợp lệ và lệnh cho kho bắt đầu nhặt hàng để đóng gói.
    //SHIPPING: Nhân viên kho đã đóng gói và giao cho Shipper (đã trừ physical_qty).
    //DELIVERED: Shipper báo đã giao hàng thành công tới tay khách.
    //CANCELLED: Khách hủy đơn trước khi xuất kho (được hoàn lại available_qty).
    //RETURNED: Khách trả hàng sau khi đã nhận, nhân viên kho đã nhận lại hộp hàng nguyên vẹn (được cộng lại physical_qty).
}
