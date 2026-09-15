package com.dtp.cosmemgt.admin.service;

import com.dtp.cosmemgt.admin.dto.request.ChangePasswordRequest;
import com.dtp.cosmemgt.admin.dto.request.ForgotPasswordRequest;
import com.dtp.cosmemgt.admin.entity.User;
import com.dtp.cosmemgt.admin.repository.UserRepository;
import com.dtp.cosmemgt.core.commonService.MailService;
import com.dtp.cosmemgt.core.exception.AppException;
import com.dtp.cosmemgt.core.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChangePasswordService {
    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    RedisTemplate<String, Object> redisTemplate;
    MailService mailService;

    public void sendForgotPasswordOtp(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        String otp = String.format("%06d", new SecureRandom().nextInt(1_000_000));
        String redisKey = "otp::forgot_pw::" + email;

        redisTemplate.opsForValue().set(redisKey, otp, Duration.ofMinutes(5));

        mailService.sendOtpForgotPasswordEmail(user.getEmail(), user.getFullName(), otp);
    }

    public void resetPassword(ForgotPasswordRequest request) {
        log.info("resetInfo: {}{}{}", request.getEmail(), request.getOtp(), request.getNewPassword());
        String redisKey = "otp::forgot_pw::" + request.getEmail();
        String savedOtp = (String) redisTemplate.opsForValue().get(redisKey);

        if (savedOtp == null || !savedOtp.equals(request.getOtp())) {
            throw new AppException(ErrorCode.INVALID_OR_EXPIRED_OTP);
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        redisTemplate.delete(redisKey);

        mailService.sendPasswordChangedSuccessEmail(user);
    }

    public void changePassword(ChangePasswordRequest request) {
        String userId = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.WRONG_PASSWORD);
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.PASSWORD_MUST_BE_DIFFERENT);
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        mailService.sendPasswordChangedSuccessEmail(user);
    }
}