package com.dtp.cosmemgt.core.utils;

import com.dtp.cosmemgt.admin.entity.User;
import com.dtp.cosmemgt.admin.repository.UserRepository;
import com.dtp.cosmemgt.core.exception.AppException;
import com.dtp.cosmemgt.core.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class Utils {
    static UserRepository userRepository;

    public static User getCurrentUser(){
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();

        return userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }
}
