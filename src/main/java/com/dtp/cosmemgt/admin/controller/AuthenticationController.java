package com.dtp.cosmemgt.admin.controller;

import com.dtp.cosmemgt.admin.dto.request.*;
import com.dtp.cosmemgt.admin.dto.response.AuthenticationResponse;
import com.dtp.cosmemgt.admin.dto.response.IntrospectResponse;
import com.dtp.cosmemgt.admin.service.AuthenticationService;
import com.dtp.cosmemgt.admin.service.ChangePasswordService;
import com.dtp.cosmemgt.core.dto.ApiResponse;
import com.nimbusds.jose.JOSEException;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {
    AuthenticationService authenticationService;
    ChangePasswordService changePasswordService;

    @PostMapping("/token")
    ApiResponse<AuthenticationResponse> authenticate(@RequestBody @Valid AuthenticationRequest request) {
        var result = authenticationService.authenticate(request);
        return ApiResponse.<AuthenticationResponse>builder().result(result).build();
    }

    @PostMapping("/introspect")
    ApiResponse<IntrospectResponse> authenticate(@RequestBody @Valid IntrospectRequest request)
            throws ParseException, JOSEException {
        var result = authenticationService.introspect(request);
        return ApiResponse.<IntrospectResponse>builder().result(result).build();
    }

    @PostMapping("/refresh")
    ApiResponse<AuthenticationResponse> authenticate(@RequestBody @Valid RefreshRequest request)
            throws ParseException, JOSEException {
        var result = authenticationService.refreshToken(request);
        return ApiResponse.<AuthenticationResponse>builder().result(result).build();
    }

    @PostMapping("/logout")
    ApiResponse<Void> logout(@RequestBody @Valid LogoutRequest request) throws ParseException, JOSEException {
        authenticationService.logout(request);
        return ApiResponse.<Void>builder().build();
    }

    @PostMapping("/forgot/send-otp")
    public ApiResponse<String> sendForgotPasswordOtp(@RequestParam String email) {
        changePasswordService.sendForgotPasswordOtp(email);
        return ApiResponse.<String>builder()
                .result("Mã OTP đã được gửi đến email của bạn.")
                .build();
    }

    @PostMapping("/forgot/reset")
    public ApiResponse<String> resetPassword(@RequestBody @Valid ForgotPasswordRequest request) {
        changePasswordService.resetPassword(request);
        return ApiResponse.<String>builder()
                .result("Đặt lại mật khẩu thành công.")
                .build();
    }

    @PutMapping("/change")
    public ApiResponse<String> changePassword(@RequestBody @Valid ChangePasswordRequest request) {
        changePasswordService.changePassword(request);
        return ApiResponse.<String>builder()
                .result("Đổi mật khẩu thành công.")
                .build();
    }
}
