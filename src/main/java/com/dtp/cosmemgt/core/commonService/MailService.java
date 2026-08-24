package com.dtp.cosmemgt.core.commonService;

import com.dtp.cosmemgt.admin.entity.User;
import com.dtp.cosmemgt.sales.order.entity.Order;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MailService {
    BrevoMailClient brevoMailClient;

    public void sendOrderConfirmRefundEmail(User user, Order order) {
        String subject = "Hoàn tiền đơn hàng - Mã đơn #" + order.getId();
        String htmlBody = String.format("""
            <div style="font-family: Arial, sans-serif; line-height: 1.6;">
                <h2>Xin chào %s,</h2>
                <p>Cảm ơn bạn đã đặt hàng tại <b>CosmeMgt</b>. Đơn hàng của bạn sẽ được hoàn tiền sau 2-3 ngày làm việc!</p>
                <ul>
                    <li><b>Mã đơn hàng:</b> %s</li>
                    <li><b>Tổng tiền:</b> %,d VNĐ</li>
                    <li><b>Trạng thái:</b> Đang hoàn tiền</li>
                </ul>
                <p>Cảm ơn bạn đã đặt hàng. Mọi thắc mắc liên hệ 1900....!</p>
            </div>
            """, user.getFullName(), order.getId(), order.getTotalAmount().longValue());

        brevoMailClient.sendEmail(user.getEmail(), user.getFullName(), subject, htmlBody);
    }

    public void sendOrderRefundEmail(User user, Order order) {
        String subject = "Hoàn tiền đơn hàng - Mã đơn #" + order.getId();
        String htmlBody = String.format("""
            <div style="font-family: Arial, sans-serif; line-height: 1.6;">
                <h2>Xin chào %s,</h2>
                <p>Cảm ơn bạn đã đặt hàng tại <b>CosmeMgt</b>. Đơn hàng của bạn đã được hoàn tiền thành công!</p>
                <ul>
                    <li><b>Mã đơn hàng:</b> %s</li>
                    <li><b>Tổng tiền:</b> %,d VNĐ</li>
                    <li><b>Trạng thái:</b> Đã hoàn tiền</li>
                </ul>
                <p>Cảm ơn bạn đã đặt hàng. Mọi thắc mắc liên hệ 1900....!</p>
            </div>
            """, user.getFullName(), order.getId(), order.getTotalAmount().longValue());

        brevoMailClient.sendEmail(user.getEmail(), user.getFullName(), subject, htmlBody);
    }

    public void sendOrderConfirmationEmail(User user, Order order) {
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

        brevoMailClient.sendEmail(user.getEmail(), user.getFullName(), subject, htmlBody);
    }
}