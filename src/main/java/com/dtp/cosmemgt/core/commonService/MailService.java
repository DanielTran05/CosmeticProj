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
                <p>Cảm ơn bạn đã đặt hàng tại <b>Yesstyle</b>. Đơn hàng của bạn sẽ được hoàn tiền sau 2-3 ngày làm việc!</p>
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
                <p>Cảm ơn bạn đã đặt hàng tại <b>Yesstyle</b>. Đơn hàng của bạn đã được hoàn tiền thành công!</p>
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

    public void sendWaitingOrderRefundEmail(User user, Order order) {
        String subject = "Hoàn tiền đơn hàng - Mã đơn #" + order.getId();
        String htmlBody = String.format("""
            <div style="font-family: Arial, sans-serif; line-height: 1.6;">
                <h2>Xin chào %s,</h2>
                <p>Cảm ơn bạn đã đặt hàng tại <b>Yesstyle</b>. Đơn hàng của bạn đăng đươcc xử lý hoàn tiền thành công!</p>
                <p>Quý khách vui lòng chờ từ 1-2 ngày làm việc</p>
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
        String subject = "Cảm ơn bạn đã đặt hàng tại Yesstyle - Mã đơn #" + order.getId();
        String htmlBody = String.format("""
            <div style="font-family: Arial, sans-serif; line-height: 1.6;">
                <h2>Xin chào %s,</h2>
                <p>Cảm ơn bạn đã đặt hàng tại <b>Yesstyle</b>. Đơn hàng của bạn đã được tiếp nhận thành công!</p>
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

    @Async
    public void sendOtpForgotPasswordEmail(String toEmail, String fullName, String otp) {
        String subject = "Mã xác thực khôi phục mật khẩu - Yesstyle";
        String htmlBody = String.format("""
            <div style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <h2>Xin chào %s,</h2>
                <p>Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn tại <b>Yesstyle</b>.</p>
                <p>Mã OTP xác thực của bạn là:</p>
                <div style="padding: 12px 24px; background-color: #f2f4f8; display: inline-block; font-size: 24px; font-weight: bold; letter-spacing: 4px; color: #2065DD; border-radius: 6px;">
                    %s
                </div>
                <p>Mã xác thực có hiệu lực trong vòng <b>5 phút</b>. Vui lòng không chia sẻ mã này cho bất kỳ ai.</p>
                <p>Nếu bạn không gửi yêu cầu này, vui lòng bỏ qua email.</p>
            </div>
            """, fullName != null ? fullName : "Quý khách", otp);

        brevoMailClient.sendEmail(toEmail, fullName != null ? fullName : "Customer", subject, htmlBody);
    }

    @Async
    public void sendPasswordChangedSuccessEmail(User user) {
        String subject = "Thông báo đổi mật khẩu thành công - Yesstyle";
        String htmlBody = String.format("""
            <div style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <h2>Xin chào %s,</h2>
                <p>Mật khẩu tài khoản của bạn tại <b>Yesstyle</b> vừa được thay đổi thành công.</p>
                <p>Nếu bạn không thực hiện hành động này, vui lòng liên hệ ngay với bộ phận hỗ trợ khách hàng để được bảo vệ tài khoản.</p>
            </div>
            """, user.getFullName());

        brevoMailClient.sendEmail(user.getEmail(), user.getFullName(), subject, htmlBody);
    }
}