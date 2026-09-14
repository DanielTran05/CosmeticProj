package com.dtp.cosmemgt.admin.service;

import com.dtp.cosmemgt.admin.constant.PredefinedRole;
import com.dtp.cosmemgt.admin.dto.request.AdminUserUpdateRequest;
import com.dtp.cosmemgt.admin.dto.request.UserCreationRequest;
import com.dtp.cosmemgt.admin.dto.request.UserUpdateRequest;
import com.dtp.cosmemgt.admin.dto.response.UserResponse;
import com.dtp.cosmemgt.admin.entity.Role;
import com.dtp.cosmemgt.admin.entity.User;
import com.dtp.cosmemgt.admin.mapper.UserMapper;
import com.dtp.cosmemgt.admin.repository.RoleRepository;
import com.dtp.cosmemgt.admin.repository.UserRepository;
import com.dtp.cosmemgt.core.commonService.CurrentUserService;
import com.dtp.cosmemgt.core.dto.PageResponse;
import com.dtp.cosmemgt.core.exception.AppException;
import com.dtp.cosmemgt.core.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.HashSet;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserService {
    UserRepository userRepository;
    RoleRepository roleRepository;
    CurrentUserService currentUserService;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;

public UserResponse createUser(UserCreationRequest request, boolean isAdmin) {
    User user = userMapper.toUser(request);
    user.setPassword(passwordEncoder.encode(request.getPassword()));

    HashSet<Role> roles = new HashSet<>();

    if (isAdmin) {
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            if (request.getRoles().contains(PredefinedRole.USER_ROLE)) {
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }
            var foundRoles = roleRepository.findAllById(request.getRoles());
            roles.addAll(foundRoles);
        } else {
            throw new AppException(ErrorCode.ROLE_NOT_EXIST);
        }
    } else {
        roleRepository.findById(PredefinedRole.USER_ROLE).ifPresent(roles::add);
    }

    user.setRoles(roles);

    try {
        user = userRepository.save(user);
    } catch (DataIntegrityViolationException exception) {
        throw new AppException(ErrorCode.USER_EXISTED);
    }

    return userMapper.toUserResponse(user);
}

    public UserResponse getMyInfo() {
        User u = currentUserService.getCurrentUser();

        return userMapper.toUserResponse(u);
    }

    public UserResponse updateUser(String userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        userMapper.updateUser(user, request);

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        return userMapper.toUserResponse(userRepository.save(user));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(String userId) {
        //userRepository.deleteById(UUID.fromString(userId));
        userRepository.deleteById(userId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public PageResponse<UserResponse> getUsers(String role, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<User> userPage;

        if (role != null && !role.equals("ALL")) {
            userPage = userRepository.findByRoles_Name(role, pageable);
        } else {
            userPage = userRepository.findAll(pageable);
        }

        return PageResponse.of(userPage.map(userMapper::toUserResponse));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse getUser(String id) {
        return userMapper.toUserResponse(
                userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void toggleUserStatus(String userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_EXISTED));

        boolean currentStatus = user.getIsActive() != null ? user.getIsActive() : true;
        user.setIsActive(!currentStatus);

        userRepository.save(user);
    }

//    public UserResponse updateUserByAdmin(String userId, AdminUserUpdateRequest request) {
//        User user = userRepository.findById(userId).orElseThrow(
//                () -> new AppException(ErrorCode.USER_NOT_EXISTED));
//
//        userMapper.adminUpdateUser(user, request);
//
//        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
//            throw new AppException(ErrorCode.UNAUTHORIZED);
//        }
//
//        return userMapper.toUserResponse(userRepository.save(user));
//    }
}